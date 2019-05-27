package com.ma.frontend.activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.ma.frontend.R;
import com.ma.frontend.Vo.ResultVo;
import com.ma.frontend.config.HttpConstant;
import com.ma.frontend.utils.HttpCallbackListener;
import com.ma.frontend.utils.HttpUtil;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText mUsernameEditText;
    private EditText mPassWordEditText;
    private Button mLoginButton;
    private TextView mRegister;
    //用于接收Http请求的servlet的URL地址
   // private String originAddress = getResources().getString(R.string.url_root) +"/user_student/login";

    public String name = " ";
    public String pwd = " ";

    String root= HttpConstant.OriginAddress;
    private String originAddress = root + "/user/login";

    final OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        initView();
        initEvent();
    }

    //用于处理消息的Handler
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String result = "";
            String ReturnMessage = (String) msg.obj;
            final ResultVo showresult = new Gson().fromJson(ReturnMessage, ResultVo.class);
            final int code = showresult.getCode();
            Log.i("code is",String.valueOf(code));

            if (code==200){
                result = "登录成功";

                Intent intent=new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(LoginActivity.this, InitActivity.class);
                startActivity(intent);
            }else if (code==400){
                result = "账号密码错误";
            }else {
                result = "请检查网络";
            }
            Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT).show();
        }
    };


    private void initView() {
        mUsernameEditText = (EditText) findViewById(R.id.user_name_input);
        mPassWordEditText = (EditText) findViewById(R.id.user_password_input);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mRegister =(TextView) findViewById(R.id.register_text);
    }

    private void initEvent() {
        mLoginButton.setOnClickListener(this);
        mRegister.setOnClickListener(this);
    }


    /**
     *@Auther kiwi
     *@Data 2019/5/19
     @param  * @param
     *@reutn void
     * 无OKhttp用法
     */
    public void login() {
        if (!isInputValid()){
            return;
        }
        String number = mUsernameEditText.getText().toString().trim();
        String password = mPassWordEditText.getText().toString().trim();
        if (number.isEmpty()) {
            Toast.makeText(LoginActivity.this, "账号不能为空！", Toast.LENGTH_LONG).show();
            return;
        }else if (password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "密码不能为空！", Toast.LENGTH_LONG).show();
            return;
        }/*else if (!admin.isChecked() && !user.isChecked()) {
            Toast.makeText(LoginActivity.this, "请选择权限！", Toast.LENGTH_LONG).show();
            return;
        }*/
        HashMap<String, String> params = new HashMap<String, String>();
     /*   if (admin.isChecked()) {
            params.put(User.POWER, "1");
        }else if (user.isChecked()) {
            params.put(User.POWER, "2");
        }*/

        try {
            //发起HTTP请求
            String compeletedURL = HttpUtil.getURLWithParams(originAddress, params); //originAddress请求地址， params请求参数
            HttpUtil.sendHttpRequest(compeletedURL, new HttpCallbackListener() {
                @Override
                public void onFinish(String response) {		//请求回调函数，response是响应的数据
                    Message message = new Message();
                    message.obj = response;
                    mHandler.sendMessage(message);    //给Handlder传递数据
                    Log.i("message is:",response);
                }

                @Override
                public void onError(Exception e) {
                    Message message = new Message();
                    message.obj = e.toString();
                    mHandler.sendMessage(message);
                    Log.i("message is:",message.obj.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *@Auther kiwi
     *@Data 2019/5/19
     @param  * @param username
     * @param password
     *@reutn void
    */
    private void LoginRequest(String username,String password)  {

        name = mUsernameEditText.getText().toString().trim();
        pwd = mPassWordEditText.getText().toString().trim();

        //建立请求表单，添加上传服务器的参数
        RequestBody formBody = new FormBody.Builder()
                .add("username",name)
                .add("password",pwd)
                .build();
        //发起请求
        final Request request = new Request.Builder()
                .url(originAddress)
                .post(formBody)
                .build();
        //新建一个线程，用于得到服务器响应的参数
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    //回调
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        //将服务器响应的参数response.body().string())发送到hanlder中，并更新ui
                        mHandler.obtainMessage(1, response.body().string()).sendToTarget();
                    } else {
                        throw new IOException("Unexpected code:" + response);
                    }
                } catch (IOException e) {
                    Toast.makeText(LoginActivity.this, "连接不上服务器，请检查网络", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void ToRegister(){
        Intent intent =new Intent(LoginActivity.this,RegisterActivity.class);
         startActivity(intent);
    }
    private boolean isInputValid() {
        //检查用户输入的合法性，这里暂且默认用户输入合法
        return true;
    }

    @Override
    public void onClick(View v) {
        name = mUsernameEditText.getText().toString().trim();
        pwd = mPassWordEditText.getText().toString().trim();
        switch (v.getId()){
            case R.id.login_button:
                LoginRequest(name,pwd);
                break;
            case R.id.register_text:
                ToRegister();
                break;

        }
    }
}
