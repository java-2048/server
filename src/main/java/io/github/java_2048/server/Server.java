package io.github.java_2048.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("InfiniteLoopStatement")
public class Server {

	public static void main(String[] args) throws IOException{
		Server server = new Server();
		server.start();
	}

	public void start() throws IOException{
		ServerSocket serverSocket = null;
		Socket socket;

		//32,64,128,256,512,1024,2048... txt 파일 만들기 추가
		fileSetting();
		try{
			serverSocket = new ServerSocket(8000);
			while(true){
				System.out.println("[클라이언트 연결대기중]");
				socket = serverSocket.accept();

				// client가 접속할때마다 새로운 스레드 생성
				ReceiveThread receiveThread = new ReceiveThread(socket);
				receiveThread.start();
			}
		}catch(IOException e){
			e.printStackTrace();
			throw e; // 예외를 다시 throw하여 상위 메서드로 전파
		}finally{
			try{
				if(serverSocket != null){
					serverSocket.close();
					System.out.println("[서버종료]");
				}
			}catch(IOException e){
				e.printStackTrace();
				System.out.println("[서버소켓통신에러]");
			}
		}
	}

	public void fileSetting(){
		File directory = new File("./score/");
		if (!directory.exists()) {
			directory.mkdirs(); // 디렉토리 생성
		}
		int i = 32;
		while(i <= 32768){
			File file = new File("./score/" + i + ".txt");
			if(file.exists()){
				System.out.println("파일이 존재합니다.");
			}else{
				try{
					BufferedWriter writer = new BufferedWriter(new FileWriter(file));
					writer.write("0");
					writer.close();
					System.out.println("create" + i + "file");
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			i = i * 2;
		}
	}
}


class ReceiveThread extends Thread {
	static int highscore = 0;
	Socket socket;
	BufferedReader in = null;
	PrintStream out = null;

	public ReceiveThread(Socket socket){
		this.socket = socket;
		try{
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintStream(socket.getOutputStream());
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	@Override
	public void run(){
		String scoreStr;
		String difficult;
		int score;
		try{
			difficult = in.readLine();
			scoreStr = in.readLine();
			score = Integer.parseInt(scoreStr);
			System.out.println(score);
			highscore = saveLoad(score, difficult);
			out.println(highscore);
			System.out.println(highscore);
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				socket.close();
				System.out.println("[클라이언트 연결종료]");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	public int saveLoad(int Score, String Difficult){
		int Highscore = 0;
		try{
			File tempFile = new File("./score/temp.txt");
			File file = new File("./score/" + Difficult + ".txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			BufferedReader reader = new BufferedReader(new FileReader(file));

			Highscore = Integer.parseInt(reader.readLine());
			if(Highscore > Score){
				writer.close();
				reader.close();
				return Highscore;
			}

			Highscore = Score;
			writer.write(Integer.toString(Highscore));

			writer.close();
			reader.close();


			if(file.delete()){
				if(!tempFile.renameTo(file)){
					System.out.println("To change the file name is failed");
				}
			}else{
				System.out.println("To delete the file is failed");
			}
			System.out.println("File is updated");
		}catch(IOException e){
			e.printStackTrace();
		}
		return Highscore;
	}
}