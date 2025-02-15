/*
 * Copyright 2013 WTFrobot. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package wtfrobot.rosjava;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.speech.RecognizerResult;
import com.iflytek.speech.SpeechListener;
import com.iflytek.speech.SpeechUser;
import com.iflytek.speech.SpeechConfig.RATE;
import com.iflytek.speech.SpeechError;
import com.iflytek.ui.RecognizerDialog;
import com.iflytek.ui.RecognizerDialogListener;
import com.iflytek.ui.UploadDialog;

import edu.cmu.pocketsphinx.demo.R;
/**
 * 识别界面,通过调用SDK中的RecognizerDialog和UploadDialog
 * 来实现上传命令词和命令词识别的功能.
 * @author iFlytek
 * @since 20120822
 */
public class IsrActivity extends Activity implements OnClickListener,
		RecognizerDialogListener {
	// private static final String TAG = "IsrDemoActivity";
	//语法ID关键字
	private static final String KEY_GRAMMAR_ID = "isr_grammar_id";
	//识别结果EditText
	private EditText mResultText;
	//缓存设置参数.
	private SharedPreferences mSharedPreferences;
	//语法文件ID
	private String mGrammarId;
	//识别Dialog
	private RecognizerDialog isrDialog = null;
	//上传Dialog
	private UploadDialog uploadDialog = null;
	public static String tmpchnstring="你好";
	public static String tmpobjstring="object";
	/**
	 * 识别界面入口函数.
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.demo);

		Button uploadButton = (Button) findViewById(android.R.id.button1);
		uploadButton.setOnClickListener(this);
		uploadButton.setText(R.string.text_upload);

		Button isrButton = (Button) findViewById(android.R.id.button2);
		isrButton.setOnClickListener(this);
		isrButton.setText(R.string.text_isr);

		mResultText = (EditText) findViewById(R.id.txt_result);
		mResultText.setHint(R.string.text_isr_hint);

		//加载保存的GrammarID
		mSharedPreferences = getSharedPreferences(getPackageName(),
				MODE_PRIVATE);
		mGrammarId = mSharedPreferences.getString(KEY_GRAMMAR_ID, null);	
		// 需要先进行用户登陆操作
		SpeechUser.getUser().login(this,null,null, "appid=" + getString(R.string.app_id), null);		
		// init recognizer dialog
		isrDialog = new RecognizerDialog(this, "appid=" + getString(R.string.app_id));
		isrDialog.setListener(this);	
		uploadDialog = new UploadDialog(this);
		uploadDialog.setListener(uploadListener);
	}
	
	private SpeechListener uploadListener = new SpeechListener()
	{
		@Override
		public void onData(byte[] arg0) {
			mGrammarId = new String(arg0);
			Editor editor = mSharedPreferences.edit();
			editor.putString(KEY_GRAMMAR_ID, mGrammarId);
			editor.commit();
		}

		@Override
		public void onEvent(int arg0, Bundle arg1) {
			
		}

		@Override
		public void onEnd(SpeechError arg0) {
		}
	};
	/**
	 * 事件点击.
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		//显示识别Dialog.
		case android.R.id.button2:
			if (TextUtils.isEmpty(mGrammarId)) {
				Dialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.title_isr_warning)
				.setMessage(R.string.message_isr_warning).create();
				dialog.show();
			} else {
				showIsrDialog();
			}
			break;
		case android.R.id.button1:
			showUploadDialog();
			break;
		default:
			break;
		}
	}
	/**
	 * 显示识别界面
	 * @param 
	 */
	public void showIsrDialog()
	{
		//设置采样率参数.
		String rate = mSharedPreferences.getString(
				getString(R.string.preference_key_isr_rate),
				getString(R.string.preference_default_isr_rate));
		if (rate.equals("rate8k"))
			isrDialog.setSampleRate(RATE.rate8k);
		else if (rate.equals("rate11k"))
			isrDialog.setSampleRate(RATE.rate11k);
		else if (rate.equals("rate16k"))
			isrDialog.setSampleRate(RATE.rate16k);
		else if (rate.equals("rate22k"))
			isrDialog.setSampleRate(RATE.rate22k);
		//设置Grammar ID
		isrDialog.setEngine(null, null, mGrammarId);
		mResultText.setText(null);
		//显示识别Dialog
		isrDialog.show();
	}
	/**
	 * 显示上传命令词Dialog
	 * @param
	 */
	public void showUploadDialog()
	{
		try {
			String fname = "keys";		
			//获取名称为keys的文件句柄.
			File sdfile = new File(
					Environment.getExternalStorageDirectory(), fname);
			String keys = "";
			String toast = ""; 
			
			// 若SD卡中存在keys命令词文件，读取内容，
			// 若SD卡不存在，则读取工程assets文件夹下keys文件，默认为股票名称
			if (sdfile.exists()) {
				keys = readStringFromInputStream(new FileInputStream(
						sdfile));
				toast = "keys from " + sdfile.getPath();
			} else {
				keys = readStringFromInputStream(getAssets().open(
						fname));
				toast = "keys from assets";
			}
			Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
			
			//设置上传的内容
			//第一个参数是上传的文本名称，可以自定义，比如"keys"
			//第二个参数是上传的文本内容，需要转成utf-8格式
			//第三个参数是上传命令词对应的param,如果是命令词固定为dtt=keylist,sub=asr
			uploadDialog.setContent("keys",keys.getBytes("utf-8"), "subject=asr,data_type=keylist");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//显示上传Dialog，开始上传操作.
		uploadDialog.show();
	}

	/**
	 * 获取字节流对应的字符串,文件默认编码为UTF-16
	 * @param inputStream
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private String readStringFromInputStream(InputStream inputStream)
			throws UnsupportedEncodingException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream, "UTF-16"));
		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		return builder.toString();
	}

	/**
	 * RecognizerDialogListener的"结束会话"回调接口.
	 * 获取会话识别状态，若error不为空，则会话出现错误.
	 * @param error
	 */
	@Override
	public void onEnd(SpeechError error) {
	}

	/**
	 * RecognizerDialogListener的"返回识别结果"回调接口.
	 * 可以通过对RecognizerResult进行解析获取识别结果的详细内容.
	 * @param results,isLast
	 */
	@Override
	public void onResults(ArrayList<RecognizerResult> results, boolean isLast) {
		StringBuilder builder = new StringBuilder();
		
		//results是ArrayList类型的对象，需要对其每一个元素进行解析.
		for (RecognizerResult recognizerResult : results) {
			builder.append(recognizerResult.text);
		}
		mResultText.append(builder);
		mResultText.setSelection(mResultText.length());
		tmpchnstring=mResultText.getText().toString();
		dispose.control(tmpchnstring);
		PocketSphinxROS.sendchnstring =true;
		PocketSphinxROS.sendenstring =true;
		//PocketSphinxROS.send=true;
	}
}
