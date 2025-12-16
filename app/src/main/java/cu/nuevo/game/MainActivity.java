package cu.spaceattack.boss;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.graphics.drawable.*;
import android.graphics.*;
import android.view.*;
import android.content.*;
import java.io.*;
import android.view.SurfaceHolder.*;
import android.media.*;
import android.animation.*;
import android.view.animation.*;

public class MainActivity extends Activity {
	FrameLayout framelayout;
	ImageView play;
	public static MusicEngine musicengine;
	GameView gameview;
	TextView history;
	ImageView img1, img2;
	private boolean isGameStarted = false;
	String[] historia = {
			"Durante mucho tiempo se pensó que estábamos solos",
			"La humanidad anhelaba el contacto con otras civilizaciones",
			"Durante mucho tiempo, se enviaron mensajes por todo el universo",
			"Finalmente alguien nos escuchó",
			"Mas sus intenciones",
			"No eran amistosas",
			"Era demasiado tarde",
			"Nuestro planeta ya estaba ubicado",
			"Las naciones se unieron en una sola",
			"Crearon la Liga de Defensa Planetaria",
			"El futuro de la humanidad era incierto",
			"Se construyó un muro para la defensa del planeta",
			"Aunque este era capaz de interceptar los asteroides enviados por el enemigo",
			"No era suficiente",
			"No podía detener un ataque directo de los alienígenas",
			"La Liga de Defensa Planetaria utilizó sus últimos recursos en el desarrollo del arma de defensa definitiva",
			"Una plataforma de defenza orvital con capacidad ofensiva para enfrentar cualquier amenaza exterior",
			"El enemigo lanzó su última oleada",
			"El todo por el todo",
			"Eres el último piloto",
			"Tienes la misión de resistir tanto como sea posible",
			"No permitas que el enemigo alcance el planeta",
			"¡¡¡Listo para la batalla!!!"

	};

	String missions[] = {
            "Elimina a 50 enemigos",
            "Elimina a 100 enemigos",
            "Elimina a 150 enemigos",
            "Recolecta 10 vidas para tu nave",
            "Recolecta 10 vidas de defensa planetaria",

	};

	SharedPreferences gameData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		gameData = getSharedPreferences("gameData", Context.MODE_PRIVATE);
		framelayout = findViewById(R.id.mainFrameLayout);
		play = findViewById(R.id.mainImageView);
		history = findViewById(R.id.mainTextView1);
		img1 = findViewById(R.id.mainImageView1);
		img2 = findViewById(R.id.mainImageView2);
		musicengine = new MusicEngine(this);

		if (getActionBar() != null) {
			getActionBar().hide();
		}

		// startGame();

		play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View p1) {
				startGame(0);
			}
		});

		history.setTypeface(Typeface.createFromAsset(getAssets(), "pixel_font.ttf"));

		if (gameData.getBoolean("history", true)) {
			mostrarHistoria();
		} else {
			startGame();
		}

		getWindow().setFlags(1024, 1024);
		//bgsound = MediaPlayer.create(MainActivity.this, R.raw.bg_history);
		//bgsound.start();
		musicengine.startBGMusic(R.raw.bg_history);
	}

	private void startGame() {
		int miss = gameData.getInt("mission", 1) - 1;
		String mission = miss < missions.length ? missions[miss] : "Elimina a 500 enemigos";
		animateTerminalText(history, mission);
		history.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View p1) {
				startGame(0);

			}
		});
	}

	private void startGame(int x) {
		isGameStarted = true;
		history.setVisibility(View.GONE);
		framelayout.removeAllViews();
		gameview = new GameView(this);
		gameview.setOnGameOverListener(new GameView.OnGameOverListener() {

			@Override
			public void onGameOver() {
				musicengine.startBGMusic(R.raw.gameover);
			}
		});
		gameview.setOnGameVictoryListener(new GameView.OnGameVictoryListener() {

			@Override
			public void onGameVictory() {
				gameData.edit().putInt("mission", gameData.getInt("mission", 1) + 1).commit();
				musicengine.startBGMusic(R.raw.bg_victory);
			}
		});
		framelayout.addView(gameview);

		ObjectAnimator anim = new ObjectAnimator();
		anim.setTarget(framelayout);
		anim.setPropertyName("alpha");
		anim.setFloatValues(0.25f, 1);
		anim.setDuration(1500);
		anim.setInterpolator(new DecelerateInterpolator());
		anim.start();

		musicengine.startBGMusic(R.raw.bg_game);
	}

	
	int index = 0;

	public void mostrarHistoria() {
		animateTerminalText(history, historia[index]);
		index = ++index != historia.length ? index : 0;

	}

	public void toast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onResume() {
		/*if (bgsound != null) {
			bgsound.start();
		}*/
		musicengine.restart();

		super.onResume();
	}

	@Override
	protected void onPause() {
		/*if (bgsound != null) {
			bgsound.pause();
		}*/
		musicengine.pause();

		super.onPause();
	}

	private void nextImage(final int id) {
		img2.setImageResource(id);
		img2.setVisibility(View.VISIBLE);
		ObjectAnimator anim = new ObjectAnimator();
		anim.setTarget(img2);
		anim.setPropertyName("alpha");
		anim.setDuration(1000);
		anim.setFloatValues(0, 1);
		anim.addListener(new Animator.AnimatorListener() {

			@Override
			public void onAnimationStart(Animator p1) {
				// TODO: Implement this method
			}

			@Override
			public void onAnimationEnd(Animator p1) {
				img1.setImageResource(id);
				img2.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationCancel(Animator p1) {
				// TODO: Implement this method
			}

			@Override
			public void onAnimationRepeat(Animator p1) {
				// TODO: Implement this method
			}
		});
		anim.start();
	}

	private void nextAction() {
		if (isGameStarted) {
			return;
		}
		if (index == 0) {
			gameData.edit().putBoolean("history", false).commit();
			startGame();
		} else {
			mostrarHistoria();
		}

		if (index == 3) {
			nextImage(R.drawable.h3);
		}

		if (index == 5) {
			nextImage(R.drawable.h2);
		}

		if (index == 7) {
			nextImage(R.drawable.h6);
		}

		if (index == 11) {
			// img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
			nextImage(R.drawable.h5);
		}
	}

	private void animateTerminalText(final TextView textView, final String fullText) {
		final Handler textHandler = new Handler();
		final int[] charIndex = { 0 };

		Runnable typeWriter = new Runnable() {
			@Override
			public void run() {
				if (charIndex[0] < fullText.length()) {
					String currentText = fullText.substring(0, charIndex[0] + 1);
					textView.setText(currentText);
					charIndex[0]++;

					// Velocidad de escritura variable para efecto más realista
					int delay = 50; // 50-80ms por caracter
					textHandler.postDelayed(this, delay);
				} else {
					textHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							nextAction();
						}
					}, 1500);
					// Efecto de parpadeo del cursor al finalizar
					// animateCursor(textView, fullText);
				}
			}
		};

		textHandler.post(typeWriter);
	}


	public static class bug extends Activity {

		String[] exceptionType = {
				"StringIndexOutOfBoundsException",
				"IndexOutOfBoundsException",
				"ArithmeticException",
				"NumberFormatException",
				"ActivityNotFoundException"

		};

		String[] errMessage = {
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
			if (intent != null) {
				errMsg = intent.getStringExtra("error");

				String[] spilt = errMsg.split("\n");
				// errMsg = spilt[0];
				try {
					for (int j = 0; j < exceptionType.length; j++) {
						if (spilt[0].contains(exceptionType[j])) {
							madeErrMsg = errMessage[j];

							int addIndex = spilt[0].indexOf(exceptionType[j]) + exceptionType[j].length();

							madeErrMsg += spilt[0].substring(addIndex, spilt[0].length());
							break;

						}
					}

					if (madeErrMsg.isEmpty())
						madeErrMsg = errMsg;
				} catch (Exception e) {
				}

			}
			final int textColor = Color.WHITE;
			final int backgroundColor = 0xff000000;
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
			linear_main.setPadding(10, 10, 10, 10);
			linear_main.setBackgroundColor(backgroundColor);

			linear_titulo.addView(linear_titulo_text);
			linear_titulo_text.addView(textview_titulo);
			linear_titulo_text.addView(textview_subtitulo);
			linear_main.addView(linear_titulo);
			linear_main.addView(linear_texto_content, new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
			linear_texto_content.addView(sr);
			linear_texto_content.setOrientation(LinearLayout.VERTICAL);
			linear_texto_content.setPadding(0, 10, 0, 10);
			sr.addView(textview_error);

			textview_titulo.setTextColor(textColor);
			textview_subtitulo.setTextColor(textColor);
			textview_error.setTextColor(textColor);

			textview_titulo.setTextSize(20);
			textview_titulo.setTypeface(null, Typeface.BOLD);
			textview_titulo.setText("): Ha ocurrido un error :(");

			textview_subtitulo.setTypeface(null, Typeface.ITALIC);
			textview_titulo.setTextSize(20);
			textview_subtitulo.setText(
					"Lamentamos informarle que hubo un error inesperado durante la ejecución de la aplicación, si éste error persiste envíe un informe al desarrollador");

			textview_error.setText(errMsg);

			Button button = new Button(this);
			button.setBackgroundColor(buttonBackgroundColor);
			button.setTextColor(textColor);
			button.setText("Salir");
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View p1) {
					finish();
				}
			});

			linear_main.addView(button);
			setContentView(linear_main);

		}
	}
	// Application class

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

					PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 11111, intent,
							PendingIntent.FLAG_ONE_SHOT);

					AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);

					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(2);

					uncaughtExceptionHandler.uncaughtException(thread, ex);
				}
			});
			super.onCreate();

		}

		private String getStackTrace(Throwable th) {
			final Writer result = new StringWriter();

			final PrintWriter printWriter = new PrintWriter(result);
			Throwable cause = th;

			while (cause != null) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}
			final String stacktraceAsString = result.toString();
			printWriter.close();

			return stacktraceAsString;
		}
	}
}
