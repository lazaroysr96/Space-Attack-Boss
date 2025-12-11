package cu.spaceattack.boss;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.graphics.drawable.*;
import android.graphics.*;
import android.view.*;
import android.animation.*;
import android.view.animation.*;
import android.content.*;
import java.io.*;
import android.view.SurfaceHolder.*;

public class MainActivity extends Activity 
{
	FrameLayout framelayout;
	ImageView play;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		framelayout = findViewById(R.id.mainFrameLayout);
		play= findViewById(R.id.mainImageView);
		framelayout.addView(new GameView(this));
		getActionBar().hide();
		
		play.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					framelayout.removeAllViews();
					framelayout.addView(new GameView(MainActivity.this));
					
					ObjectAnimator anim = new ObjectAnimator();
					anim.setTarget(p1);
					anim.setPropertyName("alpha");
					anim.setFloatValues(0.25f,1);
					anim.setDuration(500);
					anim.setInterpolator(new DecelerateInterpolator());
					anim.start();
				}
			});
			
    }
	
	
	public void toast(String str){
		Toast.makeText(this,str,Toast.LENGTH_LONG).show();
	}
	
	
	
	
	public static class bug extends Activity {

		String[] exceptionType = {
			"StringIndexOutOfBoundsException",
			"IndexOutOfBoundsException",
			"ArithmeticException",
			"NumberFormatException",
			"ActivityNotFoundException"

		};

		String[] errMessage= {
			"Invalid string operation\n",
			"Invalid list operation\n",
			"Invalid arithmetical operation\n",
			"Invalid toNumber block operation\n",
			"Invalid intent operation"
		};


		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			getActionBar().hide();

			Intent intent = getIntent();
			String errMsg = "";
			String madeErrMsg = "";
			if(intent != null){
				errMsg = intent.getStringExtra("error");

				String[] spilt = errMsg.split("\n");
				//errMsg = spilt[0];
				try {
					for (int j = 0; j < exceptionType.length; j++) {
						if (spilt[0].contains(exceptionType[j])) {
							madeErrMsg = errMessage[j];

							int addIndex = spilt[0].indexOf(exceptionType[j]) + exceptionType[j].length();

							madeErrMsg += spilt[0].substring(addIndex, spilt[0].length());
							break;

						}
					}

					if(madeErrMsg.isEmpty()) madeErrMsg = errMsg;
				}catch(Exception e){}

			}
			final int textColor = Color.WHITE;
			final int backgroundColor=0xff000000;
			final int buttonBackgroundColor = 0x20ffffff;

			LinearLayout linear_main = new LinearLayout(this);
			LinearLayout linear_titulo = new LinearLayout(this);
			LinearLayout linear_titulo_text = new LinearLayout(this);
			LinearLayout linear_texto_content = new LinearLayout(this);

			ScrollView sr = new ScrollView(this);

			TextView textview_titulo = new TextView(this);
			TextView textview_subtitulo = new TextView(this);
			final TextView textview_error = new TextView(this);

			linear_main.setOrientation(LinearLayout.VERTICAL);
			linear_titulo_text.setOrientation(LinearLayout.VERTICAL);
			linear_main.setPadding(10,10,10,10);
			linear_main.setBackgroundColor(backgroundColor);

			linear_titulo.addView(linear_titulo_text);
			linear_titulo_text.addView(textview_titulo);
			linear_titulo_text.addView(textview_subtitulo);
			linear_main.addView(linear_titulo);
			linear_main.addView(linear_texto_content,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,1));
			linear_texto_content.addView(sr);
			linear_texto_content.setOrientation(LinearLayout.VERTICAL);
			linear_texto_content.setPadding(0,10,0,10);
			sr.addView(textview_error);

			textview_titulo.setTextColor(textColor);
			textview_subtitulo.setTextColor(textColor);
			textview_error.setTextColor(textColor);

			textview_titulo.setTextSize(20);
			textview_titulo.setTypeface(null,Typeface.BOLD);
			textview_titulo.setText("): Ha ocurrido un error :(");

			textview_subtitulo.setTypeface(null,Typeface.ITALIC);
			textview_titulo.setTextSize(20);
			textview_subtitulo.setText("Lamentamos informarle que hubo un error inesperado durante la ejecución de la aplicación, si éste error persiste envíe un informe al desarrollador");

			textview_error.setText(errMsg);

			Button button = new Button(this);
			button.setBackgroundColor(buttonBackgroundColor);
			button.setTextColor(textColor);
			button.setText("Salir");
			button.setOnClickListener(new View.OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						finish();
					}
				});

			linear_main.addView(button);
			setContentView(linear_main);


		}
	}
	//Application class

	public static class app extends Application {


		private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

		@Override
		public void onCreate() {
			this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

			Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
					@Override
					public void uncaughtException(Thread thread, Throwable ex) {
						Intent intent = new Intent(getApplicationContext(), bug.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

						intent.putExtra("error", getStackTrace(ex));

						PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 11111, intent, PendingIntent.FLAG_ONE_SHOT);


						AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
						am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);

						android.os.Process.killProcess(android.os.Process.myPid());
						System.exit(2);

						uncaughtExceptionHandler.uncaughtException(thread, ex);
					}
				});
			super.onCreate();

		}


		private String getStackTrace(Throwable th){
			final Writer result = new StringWriter();

			final PrintWriter printWriter = new PrintWriter(result);
			Throwable cause = th;



			while(cause != null){
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}
			final String stacktraceAsString = result.toString();
			printWriter.close();

			return stacktraceAsString;
		}
	}
	
	/*
	public static class Power implements Parcelable
	{
		float x;
		float y;

		@Override
		public int describeContents()
		{
			// TODO: Implement this method
			return 0;
		}

		@Override
		public void writeToParcel(Parcel parcel, int p2)
		{
			parcel.writeFloat(x);
			parcel.writeFloat(x);
		}
	}
	*/
	
}
