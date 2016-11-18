package com.android.csunethelper;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.csunethelper.R;




public class MainActivity extends Activity {
	int statussign=0;		//0Ϊ�״δ򿪳���  1Ϊ����   2Ϊ����   3Ϊ��	
	int updatesign=0;		//0Ϊ�޸���   1Ϊ�и���
	String username = "";
	String password = "";
	String ip="";
	String tips="����������,�Ƿ��Զ��˳���";
	private CNHUpdate mCNHUpdate;
	private static MainActivity instance;
	private Bitmap bitmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); //������Activity
		instance = this; //��ȡӦ�ó���context
    	TextView usernametext =(TextView)findViewById(R.id.editText2);
    	TextView passwordtext =(TextView)findViewById(R.id.editText1); 
		try
		{
			SharedPreferences sharedata = getSharedPreferences("csunethelper", 0);  
			usernametext.setText(sharedata.getString("username", ""));  
			passwordtext.setText(sharedata.getString("password", ""));  
		}
		catch (Exception e){}
		
		if(usernametext.getText().length()>5) //����б����˺�����  ���Զ���½
		{
			username=usernametext.getText().toString();
    		password=passwordtext.getText().toString();

			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);  
			if(wifiManager.getWifiState()!=3)
			{
            	new AlertDialog.Builder(instance)   
	            .setTitle("��ʾ")  
	            .setMessage("��⵽WIFIδ���ӣ�������...")  
	            .setPositiveButton("�˳�", new DialogInterface.OnClickListener() { 
	                @Override 
	                public void onClick(DialogInterface dialog, int which) { 
	                	System.exit(0);
	                } 
	            })  
	            .setNegativeButton("����", null)
	            .show();  
			}
			else
			{
				new Thread(checknet).start();  //�����������߳�
			}
		}
		

	}

	private Runnable ShowImage = new Runnable()    //����һ���µ��̣߳�������ʾͼƬ
	{  
		  public void run(){
			  String imageUrl = "http://zhuhaidong.vv.si/app/image.jpg";  //ͼƬ��ַ  
		      byte data[];
			try {
				data = ImageService.getImage(imageUrl);
			      bitmap = BitmapFactory.decodeByteArray(data, 0,  data.length);
			      Message msg = new Message(); //�Ӵ��п�ʼ�����߳�ͨѶ
		            msg.what=6;
		            msg.setData(new Bundle());
		            mHandler.sendMessage(msg);		//�����߳�֮����Ҫ���͵���Ϣ
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  } 
	};
	
	private Runnable checkupdate = new Runnable()    //����һ���µ��̣߳�����������
	  {  
	    public void run()  
	    {
	      {  
	    	  try
		        {
		          	int localVersion =getVersionName(); 
		            String pathurl = "http://jw.54sh.com/cnhupdate.php?username="+username;   //�����µ�ַ
		            URL url = new URL(pathurl); 
		            HttpURLConnection conn = (HttpURLConnection)url.openConnection(); 
		            conn.setReadTimeout(1500); 
		            conn.setRequestMethod("GET"); 
		            InputStream inStream = conn.getInputStream(); 
		            int netVersion=Integer.parseInt(inputStream2String(inStream));
		            Message msg = new Message(); //�Ӵ��п�ʼ�����߳�ͨѶ
		            msg.what=1;
		            Bundle bundle = new Bundle();
		            String UpON;
		            if(netVersion>localVersion)
		            {
		            	UpON = "1";
		            }
		            else
		            {
		            	UpON = "0";
		            }
		            bundle.putString("UpON",UpON);  //��Bundle�д������   
		            msg.setData(bundle);
		            mHandler.sendMessage(msg);		//�����߳�֮����Ҫ���͵���Ϣ
		        }
		        catch (Exception e)

		        {
		        	e.printStackTrace();
		        }
	      }  
	    }  
	  };
	  
	private Runnable checknet = new Runnable()       //����һ���µ��̣߳����������������
	  {  
	    public void run()  
	    {
	      {  
	    	  try
		        {
      			
		            String pathurl = "http://www.baidu.com/"; 
		            URL url = new URL(pathurl); 
		            HttpURLConnection conn = (HttpURLConnection)url.openConnection(); 
		            conn.setConnectTimeout(3000); 
		            conn.setReadTimeout(2500); 
		            conn.setRequestMethod("GET"); 
		            InputStream inStream = conn.getInputStream(); 
		            String htmlstr = inputStream2String(inStream);
		            Message msg = new Message(); //�Ӵ��п�ʼ�����߳�ͨѶ
		            msg.what=2;
		            Bundle bundle = new Bundle();
		            String netsign;
		            if(htmlstr.indexOf("themes/default/easyui.css")>=0)
		            {
		            	netsign = "0";
		            }
		            else
		            {
		            	netsign = "1";
		            }
		            bundle.putString("netsign",netsign);  //��Bundle�д������   
		            msg.setData(bundle);
		            mHandler.sendMessage(msg);		//�����߳�֮����Ҫ���͵���Ϣ
		        }
		        catch (Exception e)
		        {
		            Message msg = new Message(); //�Ӵ��п�ʼ�����߳�ͨѶ
		            msg.what=2;
		            Bundle bundle = new Bundle();
		            bundle.putString("netsign","-1");  //��Bundle�д������   
		            msg.setData(bundle);
		            mHandler.sendMessage(msg);		//�����߳�֮����Ҫ���͵���Ϣ
		        	e.printStackTrace();
		        }
	      }  
	    }  
	  };
	  
	private Runnable postlogin = new Runnable()      //����һ���µ��̣߳�����post��½
	  {  
	    public void run()
	    {
	      {  
	    	  try {  
	    		  WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);  //��ȡWifiManager  
	    		  if(wifiManager.isWifiEnabled()) { // û����wifiʱ,ip��ַΪ0.0.0.0
			    	     WifiInfo wifiinfo= wifiManager.getConnectionInfo();  
			    	     ip = intToIp(wifiinfo.getIpAddress());  
	    		  }
	    		  String LoginUrl = "http://61.137.86.87:8080/portalNat444/AccessServices/login";  
		  		    // �������  
	    		  String LoginStr = "accountID="+username+"%40zndx.inter&password="+ RSAUtil.getRSA(password) +"&brasAddress=59df7586&userIntranetAddress="+ip;  
	              // ����URL  
	              URL url = new URL(LoginUrl);  
	              // �������  
	              HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
	              // �����������  
	              connection.setConnectTimeout(5000);  
	              connection.setReadTimeout(5000);  
	              connection.setRequestMethod("POST");// ����ʽ  
	              connection.setDoInput(true);// �ɶ�д  
	              connection.setDoOutput(true);  
	              connection.setRequestProperty("CONTENT-TYPE","application/x-www-form-urlencoded");// �������� ��������
	              connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.101 Safari/537.36");// �������� ��������  
	              connection.setRequestProperty("Host","61.137.86.87:8080");// �������� ��������  
	              connection.setRequestProperty("Origin","http://61.137.86.87:8080");// �������� ��������  
	              connection.setRequestProperty("Referer","http://61.137.86.87:8080/portalNat444/index.jsp");// �������� ��������  
	              byte[] sendData = LoginStr.getBytes("UTF-8");// �������ַ���ת��UTF-8��ʽ���ֽ�����  
	              connection.setRequestProperty("Content-Length", sendData.length+ "");// ��������ĳ���  
	              OutputStream outputStream = connection.getOutputStream();// �õ����������  
	              outputStream.write(sendData);// ����д������  
	                
	              // ��Ӧ  
	              InputStream inputStream = connection.getInputStream();  
	              InputStreamReader inputStreamReader = new InputStreamReader(  
	                      inputStream);  
	              BufferedReader bReader = new BufferedReader(inputStreamReader);  
	              String str = "";  
	              String temp = "";  
	              while ((temp = bReader.readLine()) != null) {  
	                  str = str + temp + "\n";  
	              }    
		            Message msg = new Message(); //�Ӵ��п�ʼ�����߳�ͨѶ
		            msg.what=5;
		            Bundle bundle = new Bundle();
		            bundle.putString("resdata",str);  //��Bundle�д������   
		            msg.setData(bundle);
		            mHandler.sendMessage(msg);		//�����߳�֮����Ҫ���͵���Ϣ
	          } catch (MalformedURLException e) {  
	              e.printStackTrace();  
	          } catch (IOException e) {  
	              e.printStackTrace();  
	          } catch (Exception e) {
	        	  Toast.makeText(MainActivity.this,"����δ֪����...", Toast.LENGTH_SHORT).show();
			}  
	      }  
	    }  
	  };
		  
	private Runnable postlogout = new Runnable()     //����һ���µ��̣߳�����post����
	  {  
	    public void run()  
	    {
	      {  
			    try {  
					 WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);  //��ȡWifiManager  
					 if(wifiManager.isWifiEnabled()) { // û����wifiʱ,ip��ַΪ0.0.0.0
						 WifiInfo wifiinfo= wifiManager.getConnectionInfo();  
						 ip = intToIp(wifiinfo.getIpAddress());  
					 }
		    		  String LogoutUrl = "http://61.137.86.87:8080/portalNat444/AccessServices/logout?";  
		    		  String LogoutStr = "brasAddress=59df7586&userIntranetAddress="+ip;  
		              URL url = new URL(LogoutUrl);  
		              // �������  
		              HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
		              // �����������  
		              connection.setConnectTimeout(5000);  
		              connection.setReadTimeout(5000);    
		              connection.setRequestMethod("POST");// ����ʽ  
		              connection.setDoInput(true);// �ɶ�д  
		              connection.setDoOutput(true);  
		              connection.setRequestProperty("CONTENT-TYPE","application/x-www-form-urlencoded");// �������� ��������
		              connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.101 Safari/537.36");// �������� ��������  
		              connection.setRequestProperty("Host","61.137.86.87:8080");// �������� ��������  
		              connection.setRequestProperty("Origin","http://61.137.86.87:8080");// �������� ��������  
		              connection.setRequestProperty("Referer","http://61.137.86.87:8080/portalNat444/index.jsp");// �������� ��������  
		              byte[] sendData = LogoutStr.getBytes("UTF-8");// �������ַ���ת��UTF-8��ʽ���ֽ�����  
		              connection.setRequestProperty("Content-Length", sendData.length+ "");// ��������ĳ���  
		              OutputStream outputStream = connection.getOutputStream();// �õ����������  
		              outputStream.write(sendData);// ����д������  
		                
		              // ��Ӧ  
		              InputStream inputStream = connection.getInputStream();  
		              InputStreamReader inputStreamReader = new InputStreamReader(  
		                      inputStream);  
		              BufferedReader bReader = new BufferedReader(inputStreamReader);  
		              String str = "";  
		              String temp = "";  
		              while ((temp = bReader.readLine()) != null) {  
		                  str = str + temp + "\n";  
		              }    
			            Message msg = new Message(); //�Ӵ��п�ʼ�����߳�ͨѶ
			            msg.what=5;
			            Bundle bundle = new Bundle();
			            bundle.putString("resdata",str);  //��Bundle�д������   
			            msg.setData(bundle);
			            mHandler.sendMessage(msg);		//�����߳�֮����Ҫ���͵���Ϣ
		          } catch (MalformedURLException e) {  
		              e.printStackTrace();  
		          } catch (IOException e) {  
		              e.printStackTrace();  
		          }  
	      }  
	    }  
	  };
		  
	    @SuppressLint("HandlerLeak")
		private Handler mHandler = new Handler() {      //���ܴ�����̴߳��͵���Ϣ
	        public void handleMessage (Message msg) {
	            super.handleMessage(msg);
	            if(msg.what==1)  //msg.what=1ʱΪ������ ����ΪUpON
	            {
		            String UpON = msg.getData().getString("UpON");
		            if(UpON=="1") // ��Ҫ����
		            {
			            Toast.makeText(MainActivity.this, "�������Ҫ����", Toast.LENGTH_SHORT).show();
			            mCNHUpdate = new CNHUpdate(instance);
			    		mCNHUpdate.checkUpdateInfo();
			    		updatesign=1;
		            }
	            }
	            if(msg.what==2)  //msg.what=2ʱΪ�����������  ����Ϊnetsign
	            {
	            	String netsign = msg.getData().getString("netsign");
	            	if(statussign==0)
	            	{
	            		if(netsign=="-1")
	            		{
	            			Toast.makeText(MainActivity.this,"����״���ò�...�����������԰�...", Toast.LENGTH_SHORT).show();
	            		}
	            		if(netsign=="0")
	            		{
		            		Toast.makeText(MainActivity.this,"�����Զ���½...", Toast.LENGTH_SHORT).show();
		            		new Thread(postlogin).start();
		            		statussign=1;
	            		}
	            		else
	            		{
			            	new Thread(checkupdate).start();  //�����������߳�
			            	new Thread(ShowImage).start();     //������ʾͼƬ�߳�
			            	if(updatesign==0)
			            	{
			          			
		                    	new AlertDialog.Builder(instance)   
					            .setTitle("��ʾ")  
					            .setMessage(tips)  
					            .setPositiveButton("��", new DialogInterface.OnClickListener() { 
					                @Override 
					                public void onClick(DialogInterface dialog, int which) { 
					                	System.exit(0);
					                } 
					            })  
					            .setNegativeButton("��", null)  
					            .show();  
			            	}
	            		}
	            	}
	            	else if(statussign==1)
	            	{
	            		if(netsign=="1")//�������ӳɹ�
			            {

			            	new Thread(checkupdate).start();  //�����������߳�
			            	new Thread(ShowImage).start();     //������ʾͼƬ�߳�
		                    	new AlertDialog.Builder(instance)   
					            .setTitle("��ʾ")  
					            .setMessage(tips)  
					            .setPositiveButton("��", new DialogInterface.OnClickListener() { 
					                @Override 
					                public void onClick(DialogInterface dialog, int which) { 
					                	System.exit(0);
					                } 
					            })  
					            .setNegativeButton("��", null)  
					            .show();  
			            }
			            else  //��������ʧ��
			            {
			            	//Toast.makeText(MainActivity.this,"����δ����...", Toast.LENGTH_SHORT).show();
				            new  AlertDialog.Builder(instance)    
	                        .setTitle("��ʾ")  
	                        .setMessage("��������ʧ�ܣ�" )  
	                        .setPositiveButton("ȷ��" ,  null )  		             
	                        .show();  
			            }
	            	}
	            	else if(statussign==2)
	            	{
	            		if(netsign=="1")//�������ӳɹ�
	            		{
				            new  AlertDialog.Builder(instance)    
	                        .setTitle("��ʾ")  
	                        .setMessage("����ʧ�ܣ�" )  
	                        .setPositiveButton("ȷ��" ,  null )  		             
	                        .show();  
	            		}
			            else  //��������ʧ��
			            {
	                    	new AlertDialog.Builder(instance)   
				            .setTitle("��ʾ")  
				            .setMessage("���߳ɹ�,�Ƿ��Զ��˳���")  
				            .setPositiveButton("��", new DialogInterface.OnClickListener() { 
				                @Override 
				                public void onClick(DialogInterface dialog, int which) { 
				                	System.exit(0);
				                } 
				            })  
				            .setNegativeButton("��", null)  
				            .show();  
			            }
	            	}
		            
	            }
	            
	            if(msg.what==5)  //msg.what=5ʱΪ��½  ����Ϊresstr
	            {
	            	String resdata=msg.getData().getString("resdata");
	            	if(resdata.indexOf("resultCode\":\"2")>0)
	            	{
	            		Toast.makeText(MainActivity.this,"�����ѵ�½...", Toast.LENGTH_SHORT).show();
	            	}
	            	else if(resdata.indexOf("resultCode\":\"1")>0)
	            	{
	            		if(resdata.indexOf("�����������")>0)
	            		{
	            			Toast.makeText(MainActivity.this,"�����������...", Toast.LENGTH_SHORT).show();
	            		}
	            		else if(resdata.indexOf("���˺�������")>0)
	            		{
	            			Toast.makeText(MainActivity.this,"���˺�������...", Toast.LENGTH_SHORT).show();
	            		}
	            		else if(resdata.indexOf("�˺��������")>0)
	            		{
	            			Toast.makeText(MainActivity.this,"�˺��������...", Toast.LENGTH_SHORT).show();
	            		}
	            		else
	            		{
	            			Toast.makeText(MainActivity.this,"����ԭ����֤�ܾ�...", Toast.LENGTH_SHORT).show();
	            		}
	            	}
	            	else
	            	{
	            		//Toast.makeText(MainActivity.this,resdata, Toast.LENGTH_SHORT).show();
	            		if(resdata.indexOf("resultCode\":\"0")>0)
	            		{
	            			String regEx = "((?<=(surplusmoney\":\"))[\\w\\W]*?(?=(\")))";  
	            			String s = resdata;  
	            			String rstr="";
	            			Pattern pat = Pattern.compile(regEx);  
	            			Matcher mat = pat.matcher(s);  
	            			if(mat.find()){  
	            				rstr=rstr+"ʣ����"+mat.group()+"Ԫ";
	            			} 
	            			regEx = "((?<=(usedflow\":\"))[\\w\\W]*?(?=(\")))";  
	            			pat = Pattern.compile(regEx);  
	            			mat = pat.matcher(s);  
	            			if(mat.find()){  
	            				rstr=rstr+" ��������"+mat.group()+"MB";
	            			} 
	            			regEx = "((?<=(surplusflow\":\"))[\\w\\W]*?(?=(\")))";  
	            			pat = Pattern.compile(regEx);  
	            			mat = pat.matcher(s);  
	            			if(mat.find()){  
	            				rstr=rstr+" ʣ������"+mat.group()+"MB";
	            			} 
	            			tips=rstr+",�Ƿ��˳��������";
	            		}
	            		else
	            		Toast.makeText(MainActivity.this,"�����������쳣...", Toast.LENGTH_SHORT).show();
	            	}
	            	new Thread(checknet).start();
	            }
	            if(msg.what==6)  //msg.what=6ʱΪ��ʾͼƬ
	            {
	            	ImageView imageView = (ImageView) findViewById(R.id.imageView1);
	            	imageView.setImageBitmap(bitmap);
	            }
	            
	        }
	    };
	    
	    private int getVersionName() throws Exception   //��ȡ��ǰ�汾�ź���
	    {  
	            PackageManager packageManager = getPackageManager();  
	            // getPackageName()���㵱ǰ��İ�����0�����ǻ�ȡ�汾��Ϣ  
	            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);  
	            int version = packInfo.versionCode;  
	            return version;  
	    }  
	    
	    public static String inputStream2String(InputStream   is) throws IOException{   //����ת������
	        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
	        int i=-1; 
	        while((i=is.read())!=-1){ 
	        baos.write(i); 
	        } 
	       return baos.toString(); 
	    }

	    public void calllogout(View v)  //���߰�ť����
	    {
	    	statussign=2;
            Toast.makeText(MainActivity.this,"����׼������...", Toast.LENGTH_SHORT).show();
            new Thread(postlogout).start();
	    }

	    public void calllogin(View v)   //��½��ť����
	    {
	    	statussign=1;
            Toast.makeText(MainActivity.this,"����׼����½...", Toast.LENGTH_SHORT).show();
          //���ȼ���Ƿ񱣴���Ϣ
	    	try
	    	{
	    		TextView usernametext =(TextView)findViewById(R.id.editText2);
	    		TextView passwordtext =(TextView)findViewById(R.id.editText1);
	    		username=usernametext.getText().toString();
	    		password=passwordtext.getText().toString();

	    	}
	        catch (Exception e)
	        {
	        	e.printStackTrace();
	        }
	    	CheckBox  cb = (CheckBox)findViewById(R.id.checkBox1);
	    	if(cb.isChecked())
	    	{
		    	SharedPreferences.Editor sharedata = getSharedPreferences("csunethelper", 0).edit();  
		    	sharedata.putString("username",username); 
		    	sharedata.putString("password",password); 
		    	sharedata.commit();
	    	}
	    	else
	    	{
	    		SharedPreferences.Editor sharedata = getSharedPreferences("csunethelper", 0).edit();  
	    		sharedata.putString("username",""); 
		    	sharedata.putString("password",""); 
		    	sharedata.commit();
	    	}
	    	new Thread(postlogin).start();
	    }
	    
	    public String intToIp(int i) {        //����ȡ��intת��Ϊ��׼��IP
	         
	          return (i & 0xFF ) + "." +       
	        ((i >> 8 ) & 0xFF) + "." +       
	        ((i >> 16 ) & 0xFF) + "." +       
	        ( i >> 24 & 0xFF) ;  
	     }
	    
}




