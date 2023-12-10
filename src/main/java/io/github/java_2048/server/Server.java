package io.github.java_2048.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

public class Server {

	// 메인 메소드
	public static void main(String[] args){
		Server server = new Server();
		server.start();
	}

	// 소켓을 시작하는 메[소드
	public void start(){
		ServerSocket serverSocket = null;
		Socket socket;

		fileSetting();
		try{
			// 8000 포트로 오픈
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
		}finally{
			try{
				// 소켓 닫기
				if(serverSocket != null && !serverSocket.isClosed()){
					serverSocket.close();
					System.out.println("[서버종료]");
				}
			}catch(IOException e){
				e.printStackTrace();
				System.out.println("[서버소켓통신에러]");
			}
		}
	}

	// 스코어를 저장할 파일 세팅
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

	private final Socket socket;
	private BufferedReader in = null;
	private PrintStream out = null;

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
	// 쓰레드 실행
	public void run(){
		String scoreStr;
		String difficult;
		int score;
		try{
			difficult = in.readLine();
			scoreStr = in.readLine();
			score = Integer.parseInt(scoreStr);
			System.out.println(score);
			int highscore = saveLoad(score, difficult);
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

	// temp 파일 생성
	private File createTempFile() {
		Calendar now = Calendar.getInstance();
		int m = now.get(Calendar.MINUTE);
		int s = now.get(Calendar.SECOND);
		int ms = now.get(Calendar.MILLISECOND);
		File tempFile;
		int tempN = 0;
		do {
			tempFile = new File("./score/temp" + m + s + ms + tempN + ".txt");
			tempN++;
		}while(tempFile.exists());
		return tempFile;
	}

	// 최고기록인지를 확인하고 최고기록이면 저장함
	// 최고기록을 리턴함
	private int saveLoad(int Score, String Difficult){
		int highscore = 0;
		try{
			File file = new File("./score/" + Difficult + ".txt");
			BufferedReader reader = new BufferedReader(new FileReader(file));

			highscore = Integer.parseInt(reader.readLine());
			if(highscore > Score){
				reader.close();
				return highscore;
			}

			File tempFile = createTempFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			highscore = Score;
			writer.write(Integer.toString(highscore));

			writer.close();
			reader.close();

			// 동기화를 통해 파일 수정이 다 되기를  대기
			// 파일이 없을때 읽을려고 하는 것을 방지
			synchronized(this) {
				if(file.delete()){
					if(!tempFile.renameTo(file)){
						System.out.println("To change the file name is failed");
					}
				}else{
					System.out.println("To delete the file is failed");
				}
				System.out.println("File is updated");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return highscore;
	}
}