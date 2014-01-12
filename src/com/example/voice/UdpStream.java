package com.example.voice;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;

import android.app.Activity;
import android.content.ReceiverCallNotAllowedException;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/** UdpStream activity sends and recv audio data through udp */
public class UdpStream extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udpstream);
		Button btnSend = (Button) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(LOG_TAG, "btnSend clicked");
				SendAudio();
			}
		});

		Button btnRecv = (Button) findViewById(R.id.btnRecv);
		btnRecv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(LOG_TAG, "btnRecv clicked");
				RecvAudio();
			}
		});

		Button btnStrmic = (Button) findViewById(R.id.btnStrmic);
		btnStrmic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(LOG_TAG, "btnStrmic clicked");
				SendMicAudio();
			}
		});
		
		  Button btn1send3 = (Button)findViewById(R.id.button1);
	        btn1send3.setOnClickListener(new OnClickListener() {
	                        
	                        @Override
	                        public void onClick(View v) {
	                              SendAudio2();
	                                
	                        }
	                });

	        Button btnreceive = (Button)findViewById(R.id.button2);
	        btnreceive.setOnClickListener(new OnClickListener() {
	                        
	                        @Override
	                        public void onClick(View v) {
	                            
	                                RecvAudio2();
	                        }
	                });
	        Button btncall = (Button)findViewById(R.id.button2);
	        btnreceive.setOnClickListener(new OnClickListener() {
	                        
	                        @Override
	                        public void onClick(View v) {
	                        	 SendAudio2();
	                                RecvAudio2();
	                        }
	                });
	        

	}

	static final String LOG_TAG = "UdpStream";
	static final String AUDIO_FILE_PATH = "/sdcard/1.wav";
	static final int AUDIO_PORT = 2048;
	static final int SAMPLE_RATE = 8000;
	static final int SAMPLE_INTERVAL = 15; // milliseconds
	static final int SAMPLE_SIZE = 2; // bytes per sample
	static final int BUF_SIZE = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE
			* 2;

	public void RecvAudio() {
		Thread thrd = new Thread(new Runnable() {

			@Override
			public void run() {

				// DatagramSocket dSocket = new DatagramSocket();
				DatagramChannel dChannel = null;
				try {
					dChannel = DatagramChannel.open();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				DatagramSocket dSocket = dChannel.socket();

				try {
					dSocket.setReuseAddress(true);
					dSocket.setSoTimeout(2000);
					dSocket.bind(new InetSocketAddress(2048));
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				Log.d("voice", "DatagramSocket open.");

				android.os.Process
						.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				int maxBufferSize = 4096; // my value. see what's working best
											// for you.
				int minBufferSize = AudioRecord.getMinBufferSize(11025,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
				int actualBufferSize = Math.max(minBufferSize, maxBufferSize);
				actualBufferSize = actualBufferSize * 2;

				AudioTrack aTrack;
				aTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 11025,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, actualBufferSize,
						AudioTrack.MODE_STREAM);

				DatagramPacket dPacket = new DatagramPacket(
						new byte[actualBufferSize], actualBufferSize);
				Log.d("voice", "Packet with buffersize=" + actualBufferSize);
				aTrack.play();
				Log.d("voice", "Playing track..");

				byte[] buffer = new byte[actualBufferSize];

				while (!Thread.currentThread().isInterrupted()) {
					try {
						dSocket.receive(dPacket);
						buffer = dPacket.getData();

						aTrack.setPlaybackRate(11025);
						aTrack.write(buffer, 0, buffer.length);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} // end run
		});
		thrd.start();
	}

	public void SendAudio() {
		Thread thrd = new Thread(new Runnable() {
			@Override
			public void run() {

				DatagramSocket dSocket = null;
				try {
					dSocket = new DatagramSocket();
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				android.os.Process
						.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

				Log.d("voice", "Thread starting...");
				int maxBufferSize =4096; // my value. see what's working best
											// for you.
				int minBufferSize = AudioRecord.getMinBufferSize(11025,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
				int actualBufferSize = Math.max(minBufferSize, maxBufferSize);
				actualBufferSize = actualBufferSize * 2;
				AudioRecord arec = new AudioRecord(
						MediaRecorder.AudioSource.MIC, 11025,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, actualBufferSize);

				byte[] buffer = new byte[actualBufferSize];
				Log.d("voice", "Starting to record, buffersize="
						+ actualBufferSize);
				arec.startRecording();

				while (Thread.currentThread().isAlive()
						&& !Thread.currentThread().isInterrupted()) {

					try {
						Log.d("voice", "Recording..");
						arec.read(buffer, 0, actualBufferSize);
						DatagramPacket dPacket = new DatagramPacket(buffer,
								actualBufferSize);

						dPacket.setAddress(InetAddress.getByName("192.168.1.100"));
						dPacket.setPort(2048);
						dSocket.send(dPacket);

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			} // end run
		});

		thrd.start();

	}

	public void SendMicAudio() {
		Thread thrd = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.e(LOG_TAG, "start SendMicAudio thread, thread id: "
						+ Thread.currentThread().getId());
				AudioRecord audio_recorder = new AudioRecord(
						MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT,

						AudioRecord.getMinBufferSize(SAMPLE_RATE,
								AudioFormat.CHANNEL_CONFIGURATION_MONO,
								AudioFormat.ENCODING_PCM_16BIT) * 10);
				int bytes_read = 0;
				int bytes_count = 0;
				byte[] buf = new byte[BUF_SIZE];
				try {
					InetAddress addr = InetAddress.getByName("192.168.2.3");
					DatagramSocket sock = new DatagramSocket();
					System.out.println(addr);
					while (true) {
						bytes_read = audio_recorder.read(buf, 0, BUF_SIZE);
						DatagramPacket pack = new DatagramPacket(buf,
								bytes_read, addr, AUDIO_PORT);
						sock.send(pack);
						bytes_count += bytes_read;
						Log.d(LOG_TAG, "bytes_count : " + bytes_count);
						Thread.sleep(SAMPLE_INTERVAL, 0);
					}
				} catch (InterruptedException ie) {
					Log.e(LOG_TAG, "InterruptedException");
				}
				// catch (FileNotFoundException fnfe)
				// {
				// Log.e(LOG_TAG, "FileNotFoundException");
				// }
				catch (SocketException se) {
					Log.e(LOG_TAG, "SocketException");
				} catch (UnknownHostException uhe) {
					Log.e(LOG_TAG, "UnknownHostException");
				} catch (IOException ie) {
					Log.e(LOG_TAG, "IOException");
				}
			} // end run
		});
		thrd.start();
	}
	public void SendAudio2() {
		Thread thrd = new Thread(new Runnable() {
			@Override
			public void run() {

				DatagramSocket dSocket = null;
				try {
					dSocket = new DatagramSocket();
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				android.os.Process
						.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

				Log.d("voice", "Thread starting...");
				int maxBufferSize = 4096; // my value. see what's working best
											// for you.
				int minBufferSize = AudioRecord.getMinBufferSize(11025,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
				int actualBufferSize = Math.max(minBufferSize, maxBufferSize);
				actualBufferSize = actualBufferSize * 2;
				AudioRecord arec = new AudioRecord(
						MediaRecorder.AudioSource.MIC, 11025,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, actualBufferSize);

				byte[] buffer = new byte[actualBufferSize];
				Log.d("voice", "Starting to record, buffersize="
						+ actualBufferSize);
				arec.startRecording();

				while (Thread.currentThread().isAlive()
						&& !Thread.currentThread().isInterrupted()) {

					try {
						Log.d("voice", "Recording..");
						arec.read(buffer, 0, actualBufferSize);
						DatagramPacket dPacket = new DatagramPacket(buffer,
								actualBufferSize);

						dPacket.setAddress(InetAddress.getByName("192.168.1.101"));
						dPacket.setPort(2047);
						dSocket.send(dPacket);

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			} // end run
		});

		thrd.start();

	}
	public void RecvAudio2() {
		Thread thrd = new Thread(new Runnable() {

			@Override
			public void run() {

				// DatagramSocket dSocket = new DatagramSocket();
				DatagramChannel dChannel = null;
				try {
					dChannel = DatagramChannel.open();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				DatagramSocket dSocket = dChannel.socket();

				try {
					dSocket.setReuseAddress(true);
					dSocket.setSoTimeout(2000);
					dSocket.bind(new InetSocketAddress(2047));
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				Log.d("voice", "DatagramSocket open.");

				android.os.Process
						.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				int maxBufferSize = 4096; // my value. see what's working best
											// for you.
				int minBufferSize = AudioRecord.getMinBufferSize(11025,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
				int actualBufferSize = Math.max(minBufferSize, maxBufferSize);
				actualBufferSize = actualBufferSize * 2;

				AudioTrack aTrack;
				aTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 11025,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, actualBufferSize,
						AudioTrack.MODE_STREAM);

				DatagramPacket dPacket = new DatagramPacket(
						new byte[actualBufferSize], actualBufferSize);
				Log.d("voice", "Packet with buffersize=" + actualBufferSize);
				aTrack.play();
				Log.d("voice", "Playing track..");

				byte[] buffer = new byte[actualBufferSize];

				while (!Thread.currentThread().isInterrupted()) {
					try {
						dSocket.receive(dPacket);
						buffer = dPacket.getData();

						aTrack.setPlaybackRate(11025);
						aTrack.write(buffer, 0, buffer.length);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} // end run
		});
		thrd.start();
	}
}
