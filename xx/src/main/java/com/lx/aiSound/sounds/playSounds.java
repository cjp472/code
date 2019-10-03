package com.lx.aiSound.sounds;

import java.io.*;


import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class playSounds {
	private Player player = null;
	private boolean isRepeat;
	private boolean isClosed = false;
	private byte[] audioBytes;
	private File music;

	public static void main(String[] args) throws FileNotFoundException, JavaLayerException {
		new playSounds(new FileInputStream("src/sounds/40杀.mp3"));
	}
	public playSounds(InputStream input) throws JavaLayerException {
		if(player!=null)
		{
			player.close();//先把之前的实例释放
			player = null;
		}
		player = new Player(input);
		player.play();
	}
	public playSounds(String audioFilePath, boolean isRepeat){
		this.isRepeat = isRepeat;
		music=new File("src/sounds/"+audioFilePath);
		FileInputStream inStream = null;
		try{
			inStream = new FileInputStream(music);
			byte[] buffer = new byte[(int) music.length()];
			inStream.read(buffer);
			this.audioBytes = buffer;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally
		{
			try {
				if(inStream!=null)
					inStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void doPlay()
	{
		try
		{
			if(player!=null)
			{
				player.close();//先把之前的实例释放
				player = null;
			}
			ByteArrayInputStream bytesStream = new ByteArrayInputStream(this.audioBytes);
			player = new Player(bytesStream);
			player.play();
		} catch (JavaLayerException e)
		{
			throw new RuntimeException("播放器异常", e);
		}
	}

	public void playAsync()
	{
		//Player的播放时同步的，因此放到线程中进行异步播放
		Thread playThread = new Thread(new Runnable() {
			public void run()
			{
				doPlay();
				if(!isRepeat)//如果不是重复播放，则播放一次即可
				{
					return;
				}
				while(true)//重复播放
				{
					if(isClosed)//如果已经关闭则结束
					{
						break;
					}
					if(player.isComplete())//如果播放结束了，则播放完成再播放一遍
					{
						doPlay();
					}
					try {
						new Thread().sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		playThread.setDaemon(true);
		playThread.start();
	}

	public void close()
	{
		this.isClosed = true;
		if(player!=null)
		{
			player.close();//先把之前的实例释放
			player = null;
		}

	}
}
