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
	int statussign=0;		//0为首次打开程序  1为上线   2为下线   3为空	
	int updatesign=0;		//0为无更新   1为有更新
	String username = "";
	String password = "";
	String ip="";
	String tips="网络已连接,是否自动退出？";
	private CNHUpdate mCNHUpdate;
	private static MainActivity instance;
	private Bitmap bitmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); //开启主Activity
		instance = this; //获取应用程序context
    	TextView usernametext =(TextView)findViewById(R.id.editText2);
    	TextView passwordtext =(TextView)findViewById(R.id.editText1); 
		try
		{
			SharedPreferences sharedata = getSharedPreferences("csunethelper", 0);  
			usernametext.setText(sharedata.getString("username", ""));  
			passwordtext.setText(sharedata.getString("password", ""));  
		}
		catch (Exception e){}
		
		if(usernametext.getText().length()>5) //如果有保存账号密码  则自动登陆
		{
			username=usernametext.getText().toString();
    		password=passwordtext.getText().toString();

			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);  
			if(wifiManager.getWifiState()!=3)
			{
            	new AlertDialog.Builder(instance)   
	            .setTitle("提示")  
	            .setMessage("检测到WIFI未连接，请连接...")  
	            .setPositiveButton("退出", new DialogInterface.OnClickListener() { 
	                @Override 
	                public void onClick(DialogInterface dialog, int which) { 
	                	System.exit(0);
	                } 
	            })  
	            .setNegativeButton("继续", null)
	            .show();  
			}
			else
			{
				new Thread(checknet).start();  //开启网络检查线程
			}
		}
		

	}

	private Runnable ShowImage = new Runnable()    //定义一个新的线程，用来显示图片
	{  
		  public void run(){
			  String imageUrl = "http://zhuhaidong.vv.si/app/image.jpg";  //图片地址  
		      byte data[];
			try {
				data = ImageService.getImage(imageUrl);
			      bitmap = BitmapFactory.decodeByteArray(data, 0,  data.length);
			      Message msg = new Message(); //从此行开始进行线程通讯
		            msg.what=6;
		            msg.setData(new Bundle());
		            mHandler.sendMessage(msg);		//发送线程之间需要传送的信息
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  } 
	};
	
	private Runnable checkupdate = new Runnable()    //定义一个新的线程，用来检查更新
	  {  
	    public void run()  
	    {
	      {  
	    	  try
		        {
		          	int localVersion =getVersionName(); 
		            String pathurl = "http://jw.54sh.com/cnhupdate.php?username="+username;   //检查更新地址
		            URL url = new URL(pathurl); 
		            HttpURLConnection conn = (HttpURLConnection)url.openConnection(); 
		            conn.setReadTimeout(1500); 
		            conn.setRequestMethod("GET"); 
		            InputStream inStream = conn.getInputStream(); 
		            int netVersion=Integer.parseInt(inputStream2String(inStream));
		            Message msg = new Message(); //从此行开始进行线程通讯
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
		            bundle.putString("UpON",UpON);  //往Bundle中存放数据   
		            msg.setData(bundle);
		            mHandler.sendMessage(msg);		//发送线程之间需要传送的信息
		        }
		        catch (Exception e)

		        {
		        	e.printStackTrace();
		        }
	      }  
	    }  
	  };
	  
	private Runnable checknet = new Runnable()       //定义一个新的线程，用来检查网络连接
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
		            Message msg = new Message(); //从此行开始进行线程通讯
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
		            bundle.putString("netsign",netsign);  //往Bundle中存放数据   
		            msg.setData(bundle);
		            mHandler.sendMessage(msg);		//发送线程之间需要传送的信息
		        }
		        catch (Exception e)
		        {
		            Message msg = new Message(); //从此行开始进行线程通讯
		            msg.what=2;
		            Bundle bundle = new Bundle();
		            bundle.putString("netsign","-1");  //往Bundle中存放数据   
		            msg.setData(bundle);
		            mHandler.sendMessage(msg);		//发送线程之间需要传送的信息
		        	e.printStackTrace();
		        }
	      }  
	    }  
	  };
	  
	private Runnable postlogin = new Runnable()      //定义一个新的线程，用来post登陆
	  {  
	    public void run()
	    {
	      {  
	    	  try {  
	    		  WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);  //获取WifiManager  
	    		  if(wifiManager.isWifiEnabled()) { // 没开启wifi时,ip地址为0.0.0.0
			    	     WifiInfo wifiinfo= wifiManager.getConnectionInfo();  
			    	     ip = intToIp(wifiinfo.getIpAddress());  
	    		  }
	    		  String LoginUrl = "http://61.137.86.87:8080/portalNat444/AccessServices/login";  
		  		    // 请求参数  
	    		  String LoginStr = "accountID="+username+"%40zndx.inter&password="+ RSAUtil.getRSA(password) +"&brasAddress=59df7586&userIntranetAddress="+ip;  
	              // 创建URL  
	              URL url = new URL(LoginUrl);  
	              // 获得连接  
	              HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
	              // 设置请求参数  
	              connection.setConnectTimeout(5000);  
	              connection.setReadTimeout(5000);  
	              connection.setRequestMethod("POST");// 请求方式  
	              connection.setDoInput(true);// 可读写  
	              connection.setDoOutput(true);  
	              connection.setRequestProperty("CONTENT-TYPE","application/x-www-form-urlencoded");// 设置请求 参数类型
	              connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.101 Safari/537.36");// 设置请求 参数类型  
	              connection.setRequestProperty("Host","61.137.86.87:8080");// 设置请求 参数类型  
	              connection.setRequestProperty("Origin","http://61.137.86.87:8080");// 设置请求 参数类型  
	              connection.setRequestProperty("Referer","http://61.137.86.87:8080/portalNat444/index.jsp");// 设置请求 参数类型  
	              byte[] sendData = LoginStr.getBytes("UTF-8");// 将请求字符串转成UTF-8格式的字节数组  
	              connection.setRequestProperty("Content-Length", sendData.length+ "");// 请求参数的长度  
	              OutputStream outputStream = connection.getOutputStream();// 得到输出流对象  
	              outputStream.write(sendData);// 发送写入数据  
	                
	              // 响应  
	              InputStream inputStream = connection.getInputStream();  
	              InputStreamReader inputStreamReader = new InputStreamReader(  
	                      inputStream);  
	              BufferedReader bReader = new BufferedReader(inputStreamReader);  
	              String str = "";  
	              String temp = "";  
	              while ((temp = bReader.readLine()) != null) {  
	                  str = str + temp + "\n";  
	              }    
		            Message msg = new Message(); //从此行开始进行线程通讯
		            msg.what=5;
		            Bundle bundle = new Bundle();
		            bundle.putString("resdata",str);  //往Bundle中存放数据   
		            msg.setData(bundle);
		            mHandler.sendMessage(msg);		//发送线程之间需要传送的信息
	          } catch (MalformedURLException e) {  
	              e.printStackTrace();  
	          } catch (IOException e) {  
	              e.printStackTrace();  
	          } catch (Exception e) {
	        	  Toast.makeText(MainActivity.this,"发生未知错误...", Toast.LENGTH_SHORT).show();
			}  
	      }  
	    }  
	  };
		  
	private Runnable postlogout = new Runnable()     //定义一个新的线程，用来post下线
	  {  
	    public void run()  
	    {
	      {  
			    try {  
					 WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);  //获取WifiManager  
					 if(wifiManager.isWifiEnabled()) { // 没开启wifi时,ip地址为0.0.0.0
						 WifiInfo wifiinfo= wifiManager.getConnectionInfo();  
						 ip = intToIp(wifiinfo.getIpAddress());  
					 }
		    		  String LogoutUrl = "http://61.137.86.87:8080/portalNat444/AccessServices/logout?";  
		    		  String LogoutStr = "brasAddress=59df7586&userIntranetAddress="+ip;  
		              URL url = new URL(LogoutUrl);  
		              // 获得连接  
		              HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
		              // 设置请求参数  
		              connection.setConnectTimeout(5000);  
		              connection.setReadTimeout(5000);    
		              connection.setRequestMethod("POST");// 请求方式  
		              connection.setDoInput(true);// 可读写  
		              connection.setDoOutput(true);  
		              connection.setRequestProperty("CONTENT-TYPE","application/x-www-form-urlencoded");// 设置请求 参数类型
		              connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.101 Safari/537.36");// 设置请求 参数类型  
		              connection.setRequestProperty("Host","61.137.86.87:8080");// 设置请求 参数类型  
		              connection.setRequestProperty("Origin","http://61.137.86.87:8080");// 设置请求 参数类型  
		              connection.setRequestProperty("Referer","http://61.137.86.87:8080/portalNat444/index.jsp");// 设置请求 参数类型  
		              byte[] sendData = LogoutStr.getBytes("UTF-8");// 将请求字符串转成UTF-8格式的字节数组  
		              connection.setRequestProperty("Content-Length", sendData.length+ "");// 请求参数的长度  
		              OutputStream outputStream = connection.getOutputStream();// 得到输出流对象  
		              outputStream.write(sendData);// 发送写入数据  
		                
		              // 响应  
		              InputStream inputStream = connection.getInputStream();  
		              InputStreamReader inputStreamReader = new InputStreamReader(  
		                      inputStream);  
		              BufferedReader bReader = new BufferedReader(inputStreamReader);  
		              String str = "";  
		              String temp = "";  
		              while ((temp = bReader.readLine()) != null) {  
		                  str = str + temp + "\n";  
		              }    
			            Message msg = new Message(); //从此行开始进行线程通讯
			            msg.what=5;
			            Bundle bundle = new Bundle();
			            bundle.putString("resdata",str);  //往Bundle中存放数据   
			            msg.setData(bundle);
			            mHandler.sendMessage(msg);		//发送线程之间需要传送的信息
		          } catch (MalformedURLException e) {  
		              e.printStackTrace();  
		          } catch (IOException e) {  
		              e.printStackTrace();  
		          }  
	      }  
	    }  
	  };
		  
	    @SuppressLint("HandlerLeak")
		private Handler mHandler = new Handler() {      //接受处理各线程传送的信息
	        public void handleMessage (Message msg) {
	            super.handleMessage(msg);
	            if(msg.what==1)  //msg.what=1时为检查更新 参数为UpON
	            {
		            String UpON = msg.getData().getString("UpON");
		            if(UpON=="1") // 需要升级
		            {
			            Toast.makeText(MainActivity.this, "该软件需要升级", Toast.LENGTH_SHORT).show();
			            mCNHUpdate = new CNHUpdate(instance);
			    		mCNHUpdate.checkUpdateInfo();
			    		updatesign=1;
		            }
	            }
	            if(msg.what==2)  //msg.what=2时为检查网络连接  参数为netsign
	            {
	            	String netsign = msg.getData().getString("netsign");
	            	if(statussign==0)
	            	{
	            		if(netsign=="-1")
	            		{
	            			Toast.makeText(MainActivity.this,"网络状况好差...重新连接试试吧...", Toast.LENGTH_SHORT).show();
	            		}
	            		if(netsign=="0")
	            		{
		            		Toast.makeText(MainActivity.this,"正在自动登陆...", Toast.LENGTH_SHORT).show();
		            		new Thread(postlogin).start();
		            		statussign=1;
	            		}
	            		else
	            		{
			            	new Thread(checkupdate).start();  //开启检查更新线程
			            	new Thread(ShowImage).start();     //开启显示图片线程
			            	if(updatesign==0)
			            	{
			          			
		                    	new AlertDialog.Builder(instance)   
					            .setTitle("提示")  
					            .setMessage(tips)  
					            .setPositiveButton("是", new DialogInterface.OnClickListener() { 
					                @Override 
					                public void onClick(DialogInterface dialog, int which) { 
					                	System.exit(0);
					                } 
					            })  
					            .setNegativeButton("否", null)  
					            .show();  
			            	}
	            		}
	            	}
	            	else if(statussign==1)
	            	{
	            		if(netsign=="1")//网络连接成功
			            {

			            	new Thread(checkupdate).start();  //开启检查更新线程
			            	new Thread(ShowImage).start();     //开启显示图片线程
		                    	new AlertDialog.Builder(instance)   
					            .setTitle("提示")  
					            .setMessage(tips)  
					            .setPositiveButton("是", new DialogInterface.OnClickListener() { 
					                @Override 
					                public void onClick(DialogInterface dialog, int which) { 
					                	System.exit(0);
					                } 
					            })  
					            .setNegativeButton("否", null)  
					            .show();  
			            }
			            else  //网络连接失败
			            {
			            	//Toast.makeText(MainActivity.this,"网络未连接...", Toast.LENGTH_SHORT).show();
				            new  AlertDialog.Builder(instance)    
	                        .setTitle("提示")  
	                        .setMessage("网络连接失败！" )  
	                        .setPositiveButton("确定" ,  null )  		             
	                        .show();  
			            }
	            	}
	            	else if(statussign==2)
	            	{
	            		if(netsign=="1")//网络连接成功
	            		{
				            new  AlertDialog.Builder(instance)    
	                        .setTitle("提示")  
	                        .setMessage("下线失败！" )  
	                        .setPositiveButton("确定" ,  null )  		             
	                        .show();  
	            		}
			            else  //网络连接失败
			            {
	                    	new AlertDialog.Builder(instance)   
				            .setTitle("提示")  
				            .setMessage("下线成功,是否自动退出？")  
				            .setPositiveButton("是", new DialogInterface.OnClickListener() { 
				                @Override 
				                public void onClick(DialogInterface dialog, int which) { 
				                	System.exit(0);
				                } 
				            })  
				            .setNegativeButton("否", null)  
				            .show();  
			            }
	            	}
		            
	            }
	            
	            if(msg.what==5)  //msg.what=5时为登陆  参数为resstr
	            {
	            	String resdata=msg.getData().getString("resdata");
	            	if(resdata.indexOf("resultCode\":\"2")>0)
	            	{
	            		Toast.makeText(MainActivity.this,"本机已登陆...", Toast.LENGTH_SHORT).show();
	            	}
	            	else if(resdata.indexOf("resultCode\":\"1")>0)
	            	{
	            		if(resdata.indexOf("密码输入错误")>0)
	            		{
	            			Toast.makeText(MainActivity.this,"密码输入错误...", Toast.LENGTH_SHORT).show();
	            		}
	            		else if(resdata.indexOf("该账号已在线")>0)
	            		{
	            			Toast.makeText(MainActivity.this,"该账号已在线...", Toast.LENGTH_SHORT).show();
	            		}
	            		else if(resdata.indexOf("账号输入错误")>0)
	            		{
	            			Toast.makeText(MainActivity.this,"账号输入错误...", Toast.LENGTH_SHORT).show();
	            		}
	            		else
	            		{
	            			Toast.makeText(MainActivity.this,"其他原因认证拒绝...", Toast.LENGTH_SHORT).show();
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
	            				rstr=rstr+"剩余金额"+mat.group()+"元";
	            			} 
	            			regEx = "((?<=(usedflow\":\"))[\\w\\W]*?(?=(\")))";  
	            			pat = Pattern.compile(regEx);  
	            			mat = pat.matcher(s);  
	            			if(mat.find()){  
	            				rstr=rstr+" 已用流量"+mat.group()+"MB";
	            			} 
	            			regEx = "((?<=(surplusflow\":\"))[\\w\\W]*?(?=(\")))";  
	            			pat = Pattern.compile(regEx);  
	            			mat = pat.matcher(s);  
	            			if(mat.find()){  
	            				rstr=rstr+" 剩余流量"+mat.group()+"MB";
	            			} 
	            			tips=rstr+",是否退出该软件？";
	            		}
	            		else
	            		Toast.makeText(MainActivity.this,"服务器连接异常...", Toast.LENGTH_SHORT).show();
	            	}
	            	new Thread(checknet).start();
	            }
	            if(msg.what==6)  //msg.what=6时为显示图片
	            {
	            	ImageView imageView = (ImageView) findViewById(R.id.imageView1);
	            	imageView.setImageBitmap(bitmap);
	            }
	            
	        }
	    };
	    
	    private int getVersionName() throws Exception   //获取当前版本号函数
	    {  
	            PackageManager packageManager = getPackageManager();  
	            // getPackageName()是你当前类的包名，0代表是获取版本信息  
	            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);  
	            int version = packInfo.versionCode;  
	            return version;  
	    }  
	    
	    public static String inputStream2String(InputStream   is) throws IOException{   //类型转换函数
	        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
	        int i=-1; 
	        while((i=is.read())!=-1){ 
	        baos.write(i); 
	        } 
	       return baos.toString(); 
	    }

	    public void calllogout(View v)  //下线按钮函数
	    {
	    	statussign=2;
            Toast.makeText(MainActivity.this,"正在准备下线...", Toast.LENGTH_SHORT).show();
            new Thread(postlogout).start();
	    }

	    public void calllogin(View v)   //登陆按钮函数
	    {
	    	statussign=1;
            Toast.makeText(MainActivity.this,"正在准备登陆...", Toast.LENGTH_SHORT).show();
          //首先检查是否保存信息
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
	    
	    public String intToIp(int i) {        //将获取的int转换为标准的IP
	         
	          return (i & 0xFF ) + "." +       
	        ((i >> 8 ) & 0xFF) + "." +       
	        ((i >> 16 ) & 0xFF) + "." +       
	        ( i >> 24 & 0xFF) ;  
	     }
	    
}




