package org.meaningfulweb.api;

public class StressTest {
	
	static String url = "http://127.0.0.1/~john/testMW/";
  public static void main(String[] args) throws Exception {
	final int iter = 1000000;
	int numThreads = 15;
	
	Thread[] threads = new Thread[numThreads];
	
	for (int i=0;i<threads.length;++i){
		threads[i]= new Thread(){
			public void run(){
			  try{
				MetaContentExtractor extractor = new MetaContentExtractor();
				//for (int j=0;j<iter;++j){
				int j=0;
				while(true){
				  extractor.extractFromUrl(url);
				  System.out.println(this.getId()+"..."+(j++));
				  Thread.sleep(200);
			    }
		      }
			  catch(Exception e){
				e.printStackTrace();
			  }
			}
		};
	}
	for (int i=0;i<threads.length;++i){
		threads[i].start();
	}
	for (int i=0;i<threads.length;++i){
		threads[i].join();
	}
  }
}
