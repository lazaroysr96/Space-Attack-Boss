package cu.spaceattack.boss;

import android.view.*;
import android.content.*;
import android.graphics.*;
import java.util.*;
import android.util.*;
import android.media.*;
import android.widget.*;
import android.os.*;

public class GameView extends View {

	public interface OnGameOverListener {
		void onGameOver();
	}

	public interface OnGameVictoryListener {
		void onGameVictory();
	}

	private OnGameOverListener ongameover;
	private OnGameVictoryListener ongamevictory;
	public static float basesize = 1;
	int speedAtak = 0;
	int puntos = 0;
	private int nivel = 1;
	private float x, y;
	int textColor = 0xffff0000;
	Paint paintText;

	// Paints para barras de vida
	Paint healthBarBackground;
	Paint planetHealthBar;
	Paint cannonHealthBar;
	Paint healthBarBorder;
	Nave nave = new Nave();
	ArrayList<Fire> fires = new ArrayList<>();
	ArrayList<Block> blocks = new ArrayList<>();
	ArrayList<ItemSpeed> itemSpeed = new ArrayList<>();
	ArrayList<TextFade> textfades = new ArrayList<>();
	ArrayList<ItemTriple> itemtriple = new ArrayList<>();
	ArrayList<Asteroide> asteroides = new ArrayList<>();
	ArrayList<Explocion2> explocionList = new ArrayList<>();
	ArrayList<PlusLifeNave> list_pluslife = new ArrayList<>();
	ArrayList<PlusLifeMuro> list_pluslife_muro = new ArrayList<>();
	ArrayList<Enemigo2> list_enemigo2 = new ArrayList<>();
	ArrayList<Alien1> list_alien1 = new ArrayList<>();
	ArrayList<Boss> list_boss = new ArrayList<>();
	int lastBossScore = 0;

	// contar elementos para misiones;
	int count_nave_lifes = 0;
	int count_muro_lifes = 0;
	int count_triples = 0;
	int count_speeds = 0;
	int count_nucles = 0;
	int count_jefes = 0;
	int count_enemigos = 0;

	int mision = 1;

	// padding
	int padding = 50;

	ArrayList<Item> listitem = new ArrayList<>();
	Runnable runnable, runnable2;
	Muro muro;
	N n;
	ArrayList<Explocion> list_explocion = new ArrayList<>();
	ArrayList<ItemNuclear> list_nuclear = new ArrayList<>();
	int multiatak = 0;
	boolean isGameOver = false;
	boolean isGameDone = false;
	boolean isGameVictory = false;
	boolean isEjecuteVictory = false;
	boolean isDeclareMissionDone = false;
	SharedPreferences gameData;
	private int level = 1;
	int record = 0;

	RectF rectf_game;
	ArrayList<Fire> tmp_list_fire = new ArrayList<>();

	public GameView(Context context) {
		super(context);
		init();
	}

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		gameData = getContext().getSharedPreferences("gameData", Context.MODE_PRIVATE);
		basesize = getResources().getDisplayMetrics().density;
		n = new N(basesize);
		padding = (int) (basesize * padding);
		setBackgroundResource(R.drawable.h4);
		paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintText.setTextSize(14 * basesize);
		paintText.setColor(textColor);
		paintText.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "pixel_font.ttf"));

		// Inicializar Paints para barras de vida
		healthBarBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
		healthBarBackground.setColor(0x80000000); // Gris oscuro semitransparente

		planetHealthBar = new Paint(Paint.ANTI_ALIAS_FLAG);
		planetHealthBar.setColor(0xff0080ff); // Azul para planeta

		cannonHealthBar = new Paint(Paint.ANTI_ALIAS_FLAG);
		cannonHealthBar.setColor(0xff00ff00); // Verde para cañón

		healthBarBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
		healthBarBorder.setStyle(Paint.Style.STROKE);
		healthBarBorder.setStrokeWidth(2);
		healthBarBorder.setColor(0xffffffff); // Borde blanco

		record = gameData.getInt("record", 0);
		mision = gameData.getInt("mission", 1);
		createSoundPool();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		x = w / 2;
		y = h / 2;
		rectf_game = new RectF(0, 0, w, h);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Dibujar barras de vida en la parte superior
		drawHealthBars(canvas);

		if (muro == null) {
			muro = new Muro();
		}
		muro.draw(canvas);
		generate();
		if (!nave.isEliminada()) {
			nave.draw2(canvas, getWidth(), getHeight());
		}

		gameStart(canvas);

		// Solo mostrar puntos y record si el juego no ha terminado
		if (muro != null && muro.life > 0) {
			canvas.drawText("" + puntos, getDip(25), getDip(25), paintText);
			// canvas.drawText("N.M.LIFES:
			// "+count_muro_lifes,getDip(25),getDip(50),paintText);
			canvas.drawText("F: " + fires.size(), getDip(25), getDip(75), paintText);

			isGameDone = mision == 1 ? isCompleteMission1()
					: mision == 2 ? isCompleteMission2()
							: mision == 3 ? isCompleteMission3()
									: mision == 4 ? isCompleteMission4()
											: mision == 5 ? isCompleteMission5() : (puntos >= 500) ? true : false;

			if (isGameDone) {
				if (!isDeclareMissionDone) {
					startSoundPool(8);
					isDeclareMissionDone = true;
				}
				if (blocks.size() <= 0 && list_boss.size() <= 0) {
					isGameVictory = true;
					// Notificar a MainActivity sobre la victoria
					if (ongamevictory != null) {
						if (!isEjecuteVictory) {
							ongamevictory.onGameVictory();
							isEjecuteVictory = true;
						}

					}
				}
			}

			if (isGameVictory) {
				drawCreditsScreen(canvas);
			}

		} else {

			Texto gameover = new Texto(getWidth() / 2, getHeight() / 2);
			gameover.setText("GAME OVER");
			gameover.setTextSize(getDip(40));
			gameover.setTextColor(Color.RED);
			gameover.setStrokeColor(Color.WHITE);
			gameover.setStrokeWidth(getDip(0.5f));
			gameover.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "pixel_font.ttf"));
			gameover.draw(canvas);

			gameover = new Texto(getWidth() / 2, (getHeight() / 2) - getDip(100));
			gameover.setText("PLANETA ALIEGENIZADO!!!");
			gameover.setTextSize(getDip(25));
			gameover.setTextColor(Color.BLACK);
			gameover.setStrokeColor(Color.WHITE);
			gameover.setStrokeWidth(getDip(0.5f));
			gameover.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "pixel_font.ttf"));
			gameover.draw(canvas);

			Texto tt = new Texto(getWidth() / 2, (getHeight() / 2) + getDip(50));
			tt.setText("PUNTOS " + puntos);
			tt.setTextSize(getDip(30));
			tt.setTextColor(0xffffff00);
			tt.setStrokeColor(Color.RED);
			tt.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "pixel_font.ttf"));
			tt.draw(canvas);

			if (puntos > record) {
				tt.setText("NUEVO RECORD");
				tt.y = tt.y - getDip(100);
				tt.draw(canvas);
			}

			if (ongameover != null) {
				ongameover.onGameOver();
			}

		}

		if (!isGameOver || explocionList.size() > 0 || list_explocion.size() > 0) {
			if (!isGameVictory || explocionList.size() > 0 || list_explocion.size() > 0) {
				invalidate();
			}

		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		x = event.getX();
		y = event.getY();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				event_down();
				break;
			case MotionEvent.ACTION_MOVE:
				event_move();
				break;
			case MotionEvent.ACTION_UP:
				event_up();
				break;
		}
		return true;
	}

	private void event_down() {
	}

	private void event_move() {
		// Control del cañón con el dedo - limitado al carril inferior
		if (!nave.isEliminada()) {
			// El cañón sigue la posición X del dedo pero se mantiene en el carril inferior
			nave.x = x; // Posición X sigue al dedo
			// La posición Y se mantiene fija en el carril inferior (se maneja en draw2)
		}
	}

	private void event_up() {
	}

	private void drawHealthBars(Canvas canvas) {
		float screenWidth = getWidth();
		float screenHeight = getHeight();

		// Verificar que muro no sea null antes de acceder a sus propiedades
		if (muro == null) {
			return; // Si muro es null, no dibujar barras
		}

		// Dimensiones más pequeñas y estilizadas
		float barWidth = screenWidth * 0.3f; // 30% del ancho (más pequeño)
		float barHeight = 12 * basesize; // Más delgado
		float barY = 25 * basesize; // Posición desde arriba
		float barSpacing = 15 * basesize; // Espacio entre barras

		// Posición de la barra del planeta (izquierda)
		float planetBarX = screenWidth * 0.2f;

		// Posición de la barra del cañón (derecha)
		float cannonBarX = screenWidth * 0.5f;

		// Dibujar barra de vida del Planeta (estilo paralelogramo con punta)
		drawStylizedHealthBar(canvas, planetBarX, barY, barWidth, barHeight,
				muro.life, 5, 0xff0080ff, "PLANETA");

		// Dibujar barra de vida del Cañón (estilo paralelogramo con punta)
		drawStylizedHealthBar(canvas, cannonBarX, barY, barWidth, barHeight,
				nave.life, 5, 0xff00ff00, "CAÑÓN");
	}

	private void drawStylizedHealthBar(Canvas canvas, float x, float y, float width, float height,
			int currentLife, int maxLife, int color, String label) {

		// Calcular ancho de vida
		float lifeWidth = (currentLife / (float) maxLife) * width;

		// Dibujar fondo (paralelogramo)
		Path backgroundPath = new Path();
		backgroundPath.moveTo(x, y);
		backgroundPath.lineTo(x + width - 8 * basesize, y); // Línea superior
		backgroundPath.lineTo(x + width, y + height); // Punta derecha
		backgroundPath.lineTo(x + 8 * basesize, y + height); // Línea inferior
		backgroundPath.close();

		healthBarBackground.setColor(0x60000000); // Fondo semitransparente
		canvas.drawPath(backgroundPath, healthBarBackground);

		// Dibujar barra de vida (paralelogramo con punta)
		if (lifeWidth > 8 * basesize) {
			Path lifePath = new Path();
			lifePath.moveTo(x, y);
			lifePath.lineTo(x + lifeWidth - 8 * basesize, y); // Línea superior
			lifePath.lineTo(x + lifeWidth, y + height); // Punta derecha
			lifePath.lineTo(x + 8 * basesize, y + height); // Línea inferior
			lifePath.close();

			Paint lifePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			lifePaint.setColor(color);
			canvas.drawPath(lifePath, lifePaint);
		}

		// Dibujar borde
		Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(1);
		borderPaint.setColor(0xffffffff);
		canvas.drawPath(backgroundPath, borderPaint);

		// Dibujar etiqueta pequeña
		Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		labelPaint.setTextSize(10 * basesize);
		labelPaint.setColor(0xffffffff);
		labelPaint.setTypeface(Typeface.DEFAULT_BOLD);
		canvas.drawText(label, x, y - 3 * basesize, labelPaint);

		// Dibujar puntos de vida (círculos pequeños)
		Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dotPaint.setColor(color);
		for (int i = 0; i < currentLife && i < maxLife; i++) {
			float dotX = x + (i * (width / maxLife)) + 4 * basesize;
			float dotY = y + height / 2;
			canvas.drawCircle(dotX, dotY, 2 * basesize, dotPaint);
		}
	}

	private void drawCreditsScreen(Canvas canvas) {
		float screenWidth = getWidth();
		float screenHeight = getHeight();

		// Fondo oscuro semitransparente
		Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backgroundPaint.setColor(0xCC000000);
		canvas.drawRect(0, 0, screenWidth, screenHeight, backgroundPaint);

		// Título principal
		Texto title = new Texto(screenWidth / 2, screenHeight * 0.15f);
		title.setText("¡VICTORIA!");
		title.setTextSize(getDip(50));
		title.setTextColor(Color.YELLOW);
		title.setStrokeColor(Color.WHITE);
		title.setStrokeWidth(getDip(1));
		title.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "pixel_font.ttf"));
		title.draw(canvas);

		// Subtítulo
		Texto subtitle = new Texto(screenWidth / 2, screenHeight * 0.25f);
		subtitle.setText("Has salvado el planeta");
		subtitle.setTextSize(getDip(25));
		subtitle.setTextColor(Color.CYAN);
		subtitle.setStrokeColor(Color.WHITE);
		subtitle.setStrokeWidth(getDip(0.5f));
		subtitle.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "pixel_font.ttf"));
		subtitle.draw(canvas);

		// Puntuación final
		Texto score = new Texto(screenWidth / 2, screenHeight * 0.35f);
		score.setText("PUNTUACIÓN FINAL: " + puntos);
		score.setTextSize(getDip(30));
		score.setTextColor(Color.GREEN);
		score.setStrokeColor(Color.WHITE);
		score.setStrokeWidth(getDip(0.8f));
		score.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "pixel_font.ttf"));
		score.draw(canvas);

		// Sección de créditos
		float creditsY = screenHeight * 0.45f;
		float lineSpacing = screenHeight * 0.06f;

		// Título de créditos
		Texto creditsTitle = new Texto(screenWidth / 2, creditsY);
		creditsTitle.setText("CRÉDITOS");
		creditsTitle.setTextSize(getDip(35));
		creditsTitle.setTextColor(Color.MAGENTA);
		creditsTitle.setStrokeColor(Color.WHITE);
		creditsTitle.setStrokeWidth(getDip(0.8f));
		creditsTitle.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "pixel_font.ttf"));
		creditsTitle.draw(canvas);

		// Lista de créditos
		String[] credits = {
				"Desarrollado por: Lazaro Y. S. R.",
				"Diseño y Programación",
				"Gráficos y Efectos",
				"Música y Sonidos",
				"Testing y QA",
				"Versión 1.0",
				"© 2025 Space Attack Boss"
		};

		int[] colors = {
				Color.WHITE,
				Color.CYAN,
				Color.GREEN,
				Color.YELLOW,
				Color.MAGENTA,
				Color.RED,
				Color.BLUE
		};

		for (int i = 0; i < credits.length; i++) {
			Texto creditLine = new Texto(screenWidth / 2, creditsY + lineSpacing * (i + 1));
			creditLine.setText(credits[i]);
			creditLine.setTextSize(getDip(20));
			creditLine.setTextColor(colors[i]);
			creditLine.setStrokeColor(Color.BLACK);
			creditLine.setStrokeWidth(getDip(0.3f));
			creditLine.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "pixel_font.ttf"));
			creditLine.draw(canvas);
		}

		// Mensaje final
		Texto thanks = new Texto(screenWidth / 2, screenHeight * 0.95f);
		thanks.setText("¡Gracias por jugar!");
		thanks.setTextSize(getDip(25));
		thanks.setTextColor(Color.YELLOW);
		thanks.setStrokeColor(Color.WHITE);
		thanks.setStrokeWidth(getDip(0.5f));
		thanks.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "pixel_font.ttf"));
		thanks.draw(canvas);

		// Efectos visuales adicionales
		drawVictoryEffects(canvas, screenWidth, screenHeight);
	}

	private void drawVictoryEffects(Canvas canvas, float screenWidth, float screenHeight) {
		// Estrellas parpadeantes de celebración
		Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		starPaint.setColor(Color.YELLOW);

		// Dibujar estrellas alrededor de la pantalla
		for (int i = 0; i < 20; i++) {
			float angle = (i * 18) * (float) Math.PI / 180; // Cada 18 grados
			float radius = Math.min(screenWidth, screenHeight) * 0.4f;
			float starX = screenWidth / 2 + (float) Math.cos(angle) * radius;
			float starY = screenHeight / 2 + (float) Math.sin(angle) * radius;

			// Efecto de parpadeo
			float alpha = 0.3f + 0.7f * (float) Math.abs(Math.sin(System.currentTimeMillis() / 200.0 + i));
			starPaint.setAlpha((int) (alpha * 255));

			// Dibujar estrella simple
			drawSimpleStar(canvas, starX, starY, 8 * basesize, starPaint);
		}

		// Rayos de luz desde el centro
		Paint rayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		rayPaint.setColor(Color.YELLOW);
		rayPaint.setAlpha(50);

		for (int i = 0; i < 12; i++) {
			float angle = (i * 30) * (float) Math.PI / 180;
			float startRadius = Math.min(screenWidth, screenHeight) * 0.1f;
			float endRadius = Math.min(screenWidth, screenHeight) * 0.45f;

			float startX = screenWidth / 2 + (float) Math.cos(angle) * startRadius;
			float startY = screenHeight / 2 + (float) Math.sin(angle) * startRadius;
			float endX = screenWidth / 2 + (float) Math.cos(angle) * endRadius;
			float endY = screenHeight / 2 + (float) Math.sin(angle) * endRadius;

			canvas.drawLine(startX, startY, endX, endY, rayPaint);
		}
	}

	private void drawSimpleStar(Canvas canvas, float centerX, float centerY, float size, Paint paint) {
		// Dibujar una estrella simple de 5 puntas
		Path starPath = new Path();
		for (int i = 0; i < 10; i++) {
			float angle = (i * 36 - 90) * (float) Math.PI / 180;
			float radius = (i % 2 == 0) ? size : size * 0.5f;
			float x = centerX + (float) Math.cos(angle) * radius;
			float y = centerY + (float) Math.sin(angle) * radius;

			if (i == 0) {
				starPath.moveTo(x, y);
			} else {
				starPath.lineTo(x, y);
			}
		}
		starPath.close();
		canvas.drawPath(starPath, paint);
	}

	private float getDip(float num) {
		return num * basesize;
	}

	void disparar() {
		if (nave.isEliminada()) {
			return;
		}

		if (++speedAtak >= nave.speedAtak) {
			if (multiatak > 0) {
				Fire fire1 = nave.fire();
				fire1.dx = 0.1f;
				fires.add(fire1);
				Fire fire2 = nave.fire();
				fire2.dx = -0.1f;
				fires.add(fire2);
			}
			if (multiatak > 1) {
				Fire fire3 = nave.fire();
				fire3.dx = 0.2f;
				fires.add(fire3);
				Fire fire4 = nave.fire();
				fire4.dx = -0.2f;
				fires.add(fire4);
			}
			if (multiatak > 2) {
				Fire fire5 = nave.fire();
				fire5.dx = 0.3f;
				fires.add(fire5);
				Fire fire6 = nave.fire();
				fire6.dx = -0.3f;
				fires.add(fire6);
			}

			fires.add(nave.fire());
			speedAtak = 0;

		}
	}

	int interval_block;
	int interval_fire;
	int interval_ene;
	int interval_alien1;

	public void generate() {
		if (isGameOver) {
			return;
		}

		if (!isGameDone) {
			if (++interval_fire == 50) {
				interval_fire = 0;
				Asteroide ast = new Asteroide(getRandom(50, getWidth() - 50), 0);
				ast.setSpeed(getRandom(4, 6));
				asteroides.add(ast);
			}

			if (++interval_block == 75) {
				interval_block = 0;

				Block block = new Block(getRandom(50, getWidth() - 50), 0);
				block.life = getRandom(1, level);
				block.speed = getRandom(1, nivel);
				blocks.add(block);

			}
			/*
			 * if(++interval_alien1==1000){
			 * interval_alien1=0;
			 * Alien1 aln1 = new Alien1(getRandom(padding,getWidth()-padding),0);
			 * aln1.life=5;
			 * aln1.speed=1.5f;
			 * list_alien1.add(aln1);
			 * }
			 */
			if ((puntos > 100 || mision > 2) && list_enemigo2.size() < 11) {
				if (++interval_ene == (puntos < 100 ? 500 : 200)) {
					interval_ene = 0;
					Enemigo2 ene = new Enemigo2(getRandom(padding, getWidth() - padding), 0);
					ene.life = 3;
					list_enemigo2.add(ene);
				}
			}

			if (puntos >= 50 && (puntos - lastBossScore) >= 50) {
				Boss jefe = new Boss(getRandom(padding, getWidth() - padding), 0);
				jefe.life = 10;
				list_boss.add(jefe);
				lastBossScore = puntos;
			}
		}

		if (isGameDone && (list_boss.size() <= 0) && (blocks.size() <= 0)) {
			return;
		}

		disparar();

	}

	public void gameStart(Canvas canvas) {

		for (Explocion explocion : list_explocion) {
			explocion.draw(canvas);
		}

		for (Fire fire : fires) {
			fire.draw(canvas);
		}

		for (Block block : blocks) {
			block.start(canvas);
		}

		for (Alien1 aln1 : list_alien1) {
			aln1.start(canvas);
		}

		for (Enemigo2 ene : list_enemigo2) {
			ene.move(canvas, 0, 1);
			if (ene.y > getHeight()) {
				ene.anular();
				startSoundPool(2);
			}
			// Jefe dispara (life=10)
			if (ene.life < 3 && Math.random() < 0.015) {
				// Fire fire = new Fire(ene.x + n.n12, ene.y + n.n12);
				Asteroide asteroide = new Asteroide(ene.x + n.n12, ene.y + n.n12);
				asteroides.add(asteroide);
			}
		}

		for (Boss jefe : list_boss) {
			jefe.move(canvas, 0, 1);
			if (jefe.y > getHeight()) {
				jefe.anular();
				startSoundPool(2);
			}
			// Jefe dispara
			if (jefe.life < 5 && Math.random() < 0.02) {
				Asteroide asteroide = new Asteroide(jefe.x, jefe.y);
				asteroides.add(asteroide);
				// Fire fire = new Fire(jefe.x + n.n12, jefe.y + n.n12);
				// fire.dy = 2;
				// fire.setColor(0xffff00ff);
				// fires.add(fire);
			}
		}

		for (ItemTriple it : itemtriple) {
			it.draw(canvas);

			if (isClick(nave.x, nave.y, it.x, it.y, 20 * basesize)) {
				it.eliminar();
				textfades
						.add(new TextFade("Triple fuego", (float) getWidth() / 2, (float) getHeight() / 2, 0xff00ff00));
				setMultifire();
				// startSound(R.raw.sound2);
				startSoundPool(2);
				++count_triples;
			}

			if (it.x > getHeight()) {
				it.eliminar();
			}
		}

		for (ItemSpeed is : itemSpeed) {
			is.draw(canvas);
			if (isClick(nave.x, nave.y, is.x, is.y, 20 * basesize)) {
				// startSound(R.raw.sound2);
				startSoundPool(2);
				is.eliminar();
				++count_speeds;
				if (nave.speedAtak > 15) {
					--nave.speedAtak;
					textfades.add(
							new TextFade("+ speed atak", (float) getWidth() / 2, (float) getHeight() / 2, 0xff00ff00));
				} else {
					if (runnable == null) {
						final int speed = nave.speedAtak;
						nave.speedAtak = 5;
						runnable = new Runnable() {

							@Override
							public void run() {
								nave.speedAtak = speed;
								runnable = null;
							}
						};
						getHandler().postDelayed(runnable, 3000);
					} else {
						getHandler().removeCallbacks(runnable);
						getHandler().postDelayed(runnable, 3000);
					}

					textfades.add(
							new TextFade("IPER-SPEED", (float) getWidth() / 2, (float) getHeight() / 2, 0xff00ff00));

				}
			}
			if (is.y > getHeight()) {
				is.eliminar();
			}
		}

		for (TextFade textfade : textfades) {
			textfade.draw(canvas);
		}

		for (Fire fire : fires) {
			if (!rectf_game.contains(fire.x, fire.y)) {
				fire.eliminar();
			} else {
				for (Enemigo2 ene : list_enemigo2) {
					if (!ene.anulable) {
						if (ene.rectf.contains(fire.x, fire.y)) {
							--ene.life;
							fire.eliminar();
							if (ene.life <= 0) {
								ene.anular();
								// Jefe da 50 puntos (inicialmente life=10)
								int puntosGanados = 3;
								puntos += puntosGanados;
								textfades.add(
										new TextFade("+" + puntosGanados, (float) ene.x, (float) ene.y, 0xff00ff00));
								list_explocion.add(new Explocion(fire.x, fire.y, basesize * 25, 0xffff00ff));
								startSoundPool(6);
								blocks.add(new Block(ene.x, ene.y));
							}
						}
					}
				}
				// Colisiones con jefes
				for (Boss jefe : list_boss) {
					if (!jefe.anulable) {
						if (jefe.rectf.contains(fire.x, fire.y)) {
							--jefe.life;
							fire.eliminar();

							if (jefe.life <= 0) {
								++count_jefes;
								jefe.anular();
								int puntosGanados = 5;
								puntos += puntosGanados;
								textfades.add(
										new TextFade("+" + puntosGanados, (float) jefe.x, (float) jefe.y, 0xff00ff00));
								list_explocion.add(new Explocion(fire.x, fire.y, basesize * 25, 0xffff00ff));
								startSoundPool(6);
								list_nuclear.add(new ItemNuclear(jefe.x, jefe.y));
							}
						}
					}
				}

				for (Alien1 aln : list_alien1) {
					if (!aln.isEliminada()) {
						if (isAfecte(aln.x, aln.y, aln.w, aln.h, fire.x, fire.y)) {
							if (--aln.life == 0) {
								++puntos;
								aln.eliminar();
							}
						}
					}
				}

				for (Block block : blocks) {
					if (!block.isEliminada()) {
						if (isAfecte(block.x, block.y, block.w, block.h, fire.x, fire.y)) {
							fire.eliminar();
							if (--block.life == 0) {

								switch (++puntos) {
									case 25:
										level = 2;
										break;
									case 50:
										nivel = 2;
										// startSound(R.raw.sound5);
										startSoundPool(5);
										setTextFadeCenter("NIVEL " + nivel, getColorLevel(nivel));
										break;
									case 75:
										level = 3;
										break;
									case 100:
										nivel = 3;
										// startSound(R.raw.sound5);
										startSoundPool(5);
										setTextFadeCenter("NIVEL " + nivel, getColorLevel(nivel));
										break;
									case 125:
										level = 4;
										break;
									case 150:
										nivel = 4;
										// startSound(R.raw.sound5);
										startSoundPool(5);
										setTextFadeCenter("NIVEL " + nivel, getColorLevel(nivel));
										break;
									case 175:
										level = 5;
										break;
									case 200:
										nivel = 5;
										// startSound(R.raw.sound5);
										startSoundPool(5);
										setTextFadeCenter("NIVEL " + nivel, getColorLevel(nivel));
										break;

								}
								block.eliminar();
								list_explocion.add(new Explocion(fire.x, fire.y, basesize * 25, Color.RED));
								startSoundPool(6);

								/* startSound(R.raw.sound5); */
								switch (getRandom(0, 25)) {
									case 5:
										itemSpeed.add(new ItemSpeed(fire.x, fire.y));
										break;
									case 1:
										itemtriple.add(new ItemTriple(fire.x, fire.y));
										break;
									case 7:
										list_nuclear.add(new ItemNuclear(fire.x, fire.y));
										break;
									case 11:
										list_pluslife.add(new PlusLifeNave(fire.x, fire.y));
										break;
									case 12:
										list_pluslife_muro.add(new PlusLifeMuro(fire.x, fire.y));
								}
							}
						}
					}
				}
			}
		}

		for (PlusLifeNave pl : list_pluslife) {
			pl.move(canvas);
			if (isClick(nave.x, nave.y, pl.x, pl.y, 20 * basesize)) {
				pl.consumir();
				nave.life(1);
				textfades.add(new TextFade("LIFE " + nave.life, pl.x, pl.y, 0xff00ff00));
				++count_nave_lifes;
			}
		}

		for (PlusLifeMuro plm : list_pluslife_muro) {
			plm.move(canvas);
			if (isClick(nave.x, nave.y, plm.x, plm.y, 20 * basesize)) {
				plm.consumir();
				if (muro != null) {
					++muro.life;
					textfades.add(new TextFade("LIFE " + muro.life, plm.x, plm.y, 0xff0000ff));
					++count_muro_lifes;
				}
			}
		}

		for (Block block : blocks) {
			if (muro != null && block.y > getHeight() - muro.height) {

				if (--muro.life >= 0) {

					// startSound(R.raw.sound3);
					startSoundPool(3);
					if (muro.life == 0) {
						muro.paint.setColor(0xffff0000);
						muro.paint1.setColor(0xff200000);
						if (!nave.isEliminada()) {
							naveDestroy();
						}

						isGameOver = true;
					}
				}
				block.eliminar();

			}
		}

		for (Asteroide a : asteroides) {
			a.draw(canvas);

			for (Fire fire : fires) {
				if (!a.isEliminada()) {
					if (isClick(fire.x, fire.y, a.x, a.y, a.radio)) {
						fire.eliminar();
						a.eliminar();
						startSoundPool(7);
						explocionList.add(new Explocion2(a.x, a.y));
					}
				}
			}

			if (!nave.isEliminada()) {
				if (isClick(nave.x, nave.y, a.x, a.y, (nave.bitmap.getHeight() / 2))) {
					nave.life(-1);
					explocionList.add(new Explocion2(a.x, a.y));
					a.eliminar();
					startSoundPool(1);
					if (nave.life <= 0) {
						naveDestroy();
						toast("Tu nave fue destruida");
						textfades.add(new TextFade("Tu nave explotó!!!", (float) getWidth() / 2,
								(float) getHeight() / 2, 0xffff6000));
					}
				}
			}
			if (a.y > getHeight() - muro.height) {
				explocionList.add(new Explocion2(a.x, a.y));
				a.eliminar();
			}
		}

		for (Explocion2 exp : explocionList) {
			exp.draw(canvas);
		}

		for (ItemNuclear nuclear : list_nuclear) {
			nuclear.draw(canvas);
			if (muro != null && nuclear.y > getHeight() - muro.height) {
				nuclear.eliminar();
				muro.life = 0;
				isGameOver = true;
				explocionList.add(new Explocion2(nuclear.x, nuclear.y));
				// startSound(R.raw.sound1);
				// startSound(R.raw.sound4);
				startSoundPool(1);
				startSoundPool(4);
				muro.paint.setColor(0xff101010);
			}

			for (Fire fire : fires) {
				if ((!fire.isEliminada()) && isCollisionFireNuclear(fire, nuclear)) {
					nuclear.eliminar();
					fire.eliminar();
					// startSound(R.raw.sound4);
					startSoundPool(4);
					list_explocion.add(new Explocion(nuclear.x, nuclear.y, basesize * 25, 0xffffc000));
					for (float i = -1; i < 2; i = i + 0.2f) {
						Fire tmpfire = new Fire(nuclear.x, nuclear.y);
						tmpfire.setColor(0xffffc000);
						tmpfire.dx = i;
						tmp_list_fire.add(tmpfire);

						tmpfire = new Fire(nuclear.x, nuclear.y);
						tmpfire.setColor(0xffffc000);
						tmpfire.dx = i;
						tmpfire.dy = -tmpfire.dy;
						tmp_list_fire.add(tmpfire);
					}
				}
			}
		}
		fires.addAll(tmp_list_fire);
		tmp_list_fire.clear();
		limpiar(
				fires,
				blocks,
				itemSpeed,
				textfades,
				itemtriple,
				asteroides,
				explocionList,
				list_explocion,
				list_nuclear,
				list_alien1);

		for (int i = 0; i < list_pluslife.size(); i++) {
			PlusLifeNave pl = list_pluslife.get(i);
			if (pl.consumido) {
				list_pluslife.remove(i);
			}
		}

		for (int i = 0; i < list_pluslife_muro.size(); i++) {
			PlusLifeMuro pl = list_pluslife_muro.get(i);
			if (pl.consumido) {
				list_pluslife_muro.remove(i);
			}
		}

		for (int i = 0; i < list_enemigo2.size(); i++) {
			if (list_enemigo2.get(i).anulable) {
				list_enemigo2.remove(i);
			}
		}

		for (int i = 0; i < list_boss.size(); i++) {
			if (list_boss.get(i).anulable) {
				list_boss.remove(i);
			}
		}

	}

	public void loadItem() {
		if (listitem.size() > 0) {
			String type = (String) listitem.get(0).getTag();
			int duration = listitem.get(0).getDuration();
			switch (type) {
				case "triple":

					break;
			}
		}
	}

	void setTextFadeCenter(String text, int color) {
		textfades.add(new TextFade(text, rectf_game.centerX(), rectf_game.centerY(), color));
	}

	boolean recordNow = true;

	public void naveDestroy() {

		nave.eliminar();
		explocionList.add(new Explocion2(nave.x, nave.y));

		if (recordNow) {
			if (record < puntos) {
				recordNow = false;
				gameData.edit().putInt("record", puntos).commit();
				toast("Nuevo record");
			}
		}
	}

	public void limpiar(Object... object) {
		for (Object obj : object) {
			ArrayList<UnidadBase> ub = (ArrayList<UnidadBase>) obj;
			for (int i = 0; i < ub.size(); i++) {
				if (ub.get(i).isEliminada) {
					ub.remove(i);
				}
			}
		}
	}

	private class Fire extends UnidadBase {
		public float x, y;
		private Paint paint, paint2, paint3;
		int life = 1;
		float speed = 3.5f;
		float dx = 0;
		float dy = -1;
		float animationTime = 0;
		float trailLength = 0;
		int fireType = 1; // Tipo de proyectil (1-5)

		Fire(float x, float y) {
			this(x, y, 1); // Por defecto tipo 1
		}

		Fire(float x, float y, int type) {
			this.x = x;
			this.y = y;
			this.fireType = type;
			speed = speed * basesize;

			// Paint principal - núcleo del proyectil
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(0xff00ffff); // Cyan brillante
			paint.setShadowLayer(6 * basesize, 0, 0, 0xff0088ff);

			// Paint secundario - aura energética
			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setStyle(Paint.Style.STROKE);
			paint2.setStrokeWidth(n != null ? n.get(2) : 2);
			paint2.setColor(0xffffff00); // Amarillo energético
			paint2.setAlpha(180);

			// Paint para estela - efecto de trail
			paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint3.setStyle(Paint.Style.FILL);
			paint3.setColor(0xff0080ff); // Azul eléctrico
		}

		void draw(Canvas canvas) {
			draw(canvas, fireType); // Usa el tipo específico de este proyectil
		}

		// Método principal con parámetro de tipo
		void draw(Canvas canvas, int type) {
			float mx, my;
			if (dx == 0) {
				mx = 0;
			} else {
				mx = dx * speed;
			}

			if (dy == 0) {
				my = 0;
			} else {
				my = dy * speed;
			}

			// Actualizar posición
			x = x + mx;
			y = y + my;
			animationTime += 0.1f;
			trailLength = Math.min(trailLength + 0.5f, n.n20);

			// Dibujar según tipo de proyectil
			switch (type) {
				case 1:
					drawType1(canvas, mx, my);
					break;
				case 2:
					drawType2(canvas, mx, my);
					break;
				case 3:
					drawType3(canvas, mx, my);
					break;
				case 4:
					drawType4(canvas, mx, my);
					break;
				case 5:
					drawType5(canvas, mx, my);
					break;
				default:
					drawType1(canvas, mx, my);
					break;
			}
		}

		// Tipo 1: Proyectil de energía estándar (cyan con estela)
		void drawType1(Canvas canvas, float mx, float my) {
			// Dibujar estela energética
			for (int i = 0; i < 5; i++) {
				float trailProgress = i / 5f;
				float trailX = x - mx * trailProgress * 3;
				float trailY = y - my * trailProgress * 3;
				float trailSize = (basesize * 3) * (1 - trailProgress);
				int alpha = 100 - (int) (trailProgress * 80);

				paint3.setAlpha(alpha);
				canvas.drawCircle(trailX, trailY, trailSize, paint3);
			}
			paint3.setAlpha(255);

			// Dibujar anillos expansivos
			for (int i = 0; i < 2; i++) {
				float ringProgress = (animationTime + i * 0.3f) % 1f;
				float ringSize = (basesize * 2) + ringProgress * n.n10;
				int ringAlpha = (int) ((1 - ringProgress) * 150);

				paint2.setAlpha(ringAlpha);
				canvas.drawCircle(x, y, ringSize, paint2);
			}
			paint2.setAlpha(180);

			// Dibujar núcleo principal con pulsación
			float corePulse = (basesize * 4) + (float) Math.sin(animationTime * 4) * basesize;
			canvas.drawCircle(x, y, corePulse, paint);

			// Dibujar centro brillante
			canvas.drawCircle(x, y, basesize * 2, paint2);

			// Dibujar partículas de energía
			for (int i = 0; i < 4; i++) {
				float angle = animationTime * 3 + (i * (float) Math.PI / 2);
				float particleX = x + (float) Math.cos(angle) * basesize * 2;
				float particleY = y + (float) Math.sin(angle) * basesize * 2;
				canvas.drawCircle(particleX, particleY, basesize, paint);
			}
		}

		// Tipo 2: Proyectil de láser (rojo lineal)
		void drawType2(Canvas canvas, float mx, float my) {
			// Dibujar línea de láser principal
			Paint laserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			laserPaint.setStyle(Paint.Style.STROKE);
			laserPaint.setStrokeWidth(4 * basesize);
			laserPaint.setColor(0xffff0000);
			laserPaint.setShadowLayer(8 * basesize, 0, 0, 0xff880000);

			float laserLength = 30 * basesize;
			float endX = x - mx * 2;
			float endY = y - my * 2;

			canvas.drawLine(x, y, endX, endY, laserPaint);

			// Dibujar puntos de energía en los extremos
			laserPaint.setStyle(Paint.Style.FILL);
			canvas.drawCircle(x, y, basesize * 3, laserPaint);
			canvas.drawCircle(endX, endY, basesize * 2, laserPaint);

			// Efecto de brillo
			Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			glowPaint.setColor(0xffff6666);
			glowPaint.setAlpha(100);
			canvas.drawCircle(x, y, basesize * 6, glowPaint);
		}

		// Tipo 3: Proyectil de plasma (verde esférico)
		void drawType3(Canvas canvas, float mx, float my) {
			// Dibujar esfera de plasma principal
			float plasmaSize = (basesize * 5) + (float) Math.sin(animationTime * 3) * basesize;
			Paint plasmaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			plasmaPaint.setStyle(Paint.Style.FILL);
			plasmaPaint.setColor(0xff00ff00);
			plasmaPaint.setShadowLayer(10 * basesize, 0, 0, 0xff00aa00);

			canvas.drawCircle(x, y, plasmaSize, plasmaPaint);

			// Dibujar núcleo brillante
			Paint corePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			corePaint.setStyle(Paint.Style.FILL);
			corePaint.setColor(0xffffff00);
			canvas.drawCircle(x, y, basesize * 2, corePaint);

			// Dibujar arcos eléctricos
			Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			arcPaint.setStyle(Paint.Style.STROKE);
			arcPaint.setStrokeWidth(2 * basesize);
			arcPaint.setColor(0xaaffff00);

			for (int i = 0; i < 3; i++) {
				float angle = animationTime * 2 + (i * 120) * (float) Math.PI / 180;
				float arcRadius = plasmaSize + basesize * 2;
				float arcX = x + (float) Math.cos(angle) * arcRadius;
				float arcY = y + (float) Math.sin(angle) * arcRadius;

				canvas.drawLine(x, y, arcX, arcY, arcPaint);
			}
		}

		// Tipo 4: Proyectil de hielo (azul cristalino)
		void drawType4(Canvas canvas, float mx, float my) {
			// Dibujar cristal de hielo
			Paint icePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			icePaint.setStyle(Paint.Style.FILL);
			icePaint.setColor(0xff00ccff);
			icePaint.setAlpha(200);

			// Forma hexagonal de cristal
			Path hexagon = new Path();
			float hexSize = basesize * 4;
			for (int i = 0; i < 6; i++) {
				float angle = i * 60 * (float) Math.PI / 180;
				float hexX = x + (float) Math.cos(angle) * hexSize;
				float hexY = y + (float) Math.sin(angle) * hexSize;

				if (i == 0) {
					hexagon.moveTo(hexX, hexY);
				} else {
					hexagon.lineTo(hexX, hexY);
				}
			}
			hexagon.close();

			canvas.drawPath(hexagon, icePaint);

			// Dibujar facetas internas
			icePaint.setColor(0xffffff88);
			for (int i = 0; i < 3; i++) {
				float angle = i * 120 * (float) Math.PI / 180;
				float facetX = x + (float) Math.cos(angle) * hexSize * 0.5f;
				float facetY = y + (float) Math.sin(angle) * hexSize * 0.5f;
				canvas.drawCircle(facetX, facetY, basesize, icePaint);
			}

			// Efecto de escarcha
			Paint frostPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			frostPaint.setStyle(Paint.Style.STROKE);
			frostPaint.setStrokeWidth(1 * basesize);
			frostPaint.setColor(0xccffffff);
			canvas.drawPath(hexagon, frostPaint);
		}

		// Tipo 5: Proyectil de fuego (naranja explosivo)
		void drawType5(Canvas canvas, float mx, float my) {
			// Dibujar bola de fuego
			Paint firePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			firePaint.setStyle(Paint.Style.FILL);

			// Capas de fuego con diferentes tonos
			float fireSize = (basesize * 6) + (float) Math.sin(animationTime * 5) * basesize * 2;

			// Capa exterior (rojo)
			firePaint.setColor(0xffff0000);
			firePaint.setAlpha(150);
			canvas.drawCircle(x, y, fireSize, firePaint);

			// Capa media (naranja)
			firePaint.setColor(0xffff8800);
			firePaint.setAlpha(200);
			canvas.drawCircle(x, y, fireSize * 0.8f, firePaint);

			// Capa interna (amarillo)
			firePaint.setColor(0xffffff00);
			firePaint.setAlpha(255);
			canvas.drawCircle(x, y, fireSize * 0.5f, firePaint);

			// Partículas de chispas
			Paint sparkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			sparkPaint.setStyle(Paint.Style.FILL);
			sparkPaint.setColor(0xffffff88);

			for (int i = 0; i < 8; i++) {
				float angle = animationTime * 4 + (i * 45) * (float) Math.PI / 180;
				float sparkRadius = fireSize + (float) Math.random() * basesize * 3;
				float sparkX = x + (float) Math.cos(angle) * sparkRadius;
				float sparkY = y + (float) Math.sin(angle) * sparkRadius;
				float sparkSize = basesize * (0.5f + (float) Math.random() * 1.5f);

				canvas.drawCircle(sparkX, sparkY, sparkSize, sparkPaint);
			}
		}

		void setColor(int color) {
			paint.setColor(color);
			// Ajustar colores secundarios según el color principal
			if (color == Color.RED) {
				paint2.setColor(0xffff6600); // Naranja
				paint3.setColor(0xffcc0000); // Rojo oscuro
			} else if (color == 0xffff0000) { // Rojo brillante (enemigos)
				paint2.setColor(0xffffaa00); // Amarillo anaranjado
				paint3.setColor(0xff880000); // Rojo oscuro
			} else {
				// Colores por defecto para jugador
				paint2.setColor(0xffffff00); // Amarillo
				paint3.setColor(0xff0080ff); // Azul
			}
		}
	}

	// Esto es una copia de seguridad para unenemigo basico futuro
	private class Alien1 extends UnidadBase {
		int life = 5;
		public float x, y;
		private Paint paint, paint2, paint3, paint4, paint5;
		float speed = 1;
		float w, h;
		float round;
		float animationTime = 0;
		float rotationAngle = 0;
		float tentacleWave = 0;

		float var_5, var_10, var_15, var_20, var_1, var_2, var_3, var_8, var_12;

		Alien1(float x, float y) {
			this.x = x;
			this.y = y;
			w = basesize * 25;
			h = basesize * 25;
			round = basesize * 5;

			// Paint principal - cuerpo orgánico alienígena
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(0xff4a148c); // Púrpura oscuro alienígena

			// Paint secundario - venas y detalles biológicos
			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setStyle(Paint.Style.STROKE);
			paint2.setStrokeWidth(n != null ? n.get(2) : 2);
			paint2.setColor(0xff9c27b0); // Púrpura brillante
			paint2.setShadowLayer(4 * basesize, 0, 0, 0xff6a1b9a);

			// Paint para núcleo biológico
			paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint3.setStyle(Paint.Style.FILL);
			paint3.setColor(0xff00e676); // Verde bioluminiscente
			paint3.setShadowLayer(8 * basesize, 0, 0, 0xff00c853);

			// Paint para tentáculos
			paint4 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint4.setStyle(Paint.Style.STROKE);
			paint4.setStrokeWidth(n != null ? n.get(3) : 3);
			paint4.setColor(0xff7b1fa2); // Púrpura medio
			paint4.setShadowLayer(3 * basesize, 0, 0, 0xff4a148c);

			// Paint para ojos sensores
			paint5 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint5.setStyle(Paint.Style.FILL);
			paint5.setColor(0xffff00ff); // Magenta brillante
			paint5.setShadowLayer(5 * basesize, 0, 0, 0xff000000);

			var_1 = basesize * 1;
			var_2 = basesize * 2;
			var_3 = basesize * 3;
			var_5 = basesize * 5;
			var_8 = basesize * 8;
			var_10 = basesize * 10;
			var_12 = basesize * 12;
			var_15 = basesize * 15;
			var_20 = basesize * 20;
		}

		public void start(Canvas canvas) {
			animationTime += 0.05f;
			rotationAngle += 0.02f;
			tentacleWave += 0.03f;

			// Actualizar color según vida - tonos biológicos
			int[] colors = {
					0xff4a148c, // Púrpura oscuro (vida 5)
					0xff6a1b9a, // Púrpura medio (vida 4)
					0xff7b1fa2, // Púrpura claro (vida 3)
					0xff8e24aa, // Púrpura más claro (vida 2)
					0xff9c27b0 // Púrpura brillante (vida 1)
			};
			paint.setColor(life > 0 && life <= colors.length ? colors[life - 1] : colors[0]);

			y = y + speed * basesize;

			// Dibujar cuerpo principal alienígena (forma orgánica)
			Path alienBody = new Path();
			float centerX = x + w / 2;
			float centerY = y + h / 2;

			// Forma ovalada alienígena con ondulaciones
			alienBody.moveTo(centerX - var_12, centerY - var_15);
			alienBody.quadTo(centerX - var_20, centerY, centerX - var_12, centerY + var_15);
			alienBody.quadTo(centerX, centerY + var_20, centerX + var_12, centerY + var_15);
			alienBody.quadTo(centerX + var_20, centerY, centerX + var_12, centerY - var_15);
			alienBody.quadTo(centerX, centerY - var_20, centerX - var_12, centerY - var_15);
			alienBody.close();

			canvas.drawPath(alienBody, paint);
			canvas.drawPath(alienBody, paint2);

			// Dibujar tentáculos moviéndose
			for (int i = 0; i < 6; i++) {
				float tentacleAngle = (i * (float) Math.PI / 3) + tentacleWave;
				float tentacleLength = var_15 + (float) Math.sin(tentacleWave * 2 + i) * var_3;

				float startX = centerX + (float) Math.cos(tentacleAngle) * var_8;
				float startY = centerY + (float) Math.sin(tentacleAngle) * var_8;

				float endX = centerX + (float) Math.cos(tentacleAngle) * tentacleLength;
				float endY = centerY + (float) Math.sin(tentacleAngle) * tentacleLength;

				// Tentáculo curvo
				Path tentacle = new Path();
				tentacle.moveTo(startX, startY);
				float controlX = centerX + (float) Math.cos(tentacleAngle + 0.3f) * (tentacleLength * 0.6f);
				float controlY = centerY + (float) Math.sin(tentacleAngle + 0.3f) * (tentacleLength * 0.6f);
				tentacle.quadTo(controlX, controlY, endX, endY);

				canvas.drawPath(tentacle, paint4);

				// Ventas en tentáculos
				for (int j = 0; j < 3; j++) {
					float veinProgress = j / 3f;
					float veinX = startX + (endX - startX) * veinProgress;
					float veinY = startY + (endY - startY) * veinProgress;
					canvas.drawCircle(veinX, veinY, var_1, paint3);
				}
			}

			// Dibujar núcleo biológico pulsante
			float corePulse = var_5 + (float) Math.sin(animationTime * 3) * var_2;
			canvas.drawCircle(centerX, centerY, corePulse, paint3);
			canvas.drawCircle(centerX, centerY, var_3, paint2);

			// Dibujar ojos sensores alienígenas
			for (int i = 0; i < 3; i++) {
				float eyeAngle = rotationAngle + (i * (float) Math.PI * 2 / 3);
				float eyeDistance = var_10;
				float eyeX = centerX + (float) Math.cos(eyeAngle) * eyeDistance;
				float eyeY = centerY + (float) Math.sin(eyeAngle) * eyeDistance;

				// Ojo con parpadeo
				float eyeSize = var_3 + (float) Math.sin(animationTime * 6 + i) * var_1;
				canvas.drawCircle(eyeX, eyeY, eyeSize, paint5);
				canvas.drawCircle(eyeX, eyeY, var_1, paint3);

				// Conexión neuronal al centro
				Path neuralPath = new Path();
				neuralPath.moveTo(centerX, centerY);
				neuralPath.quadTo(
						centerX + (float) Math.cos(eyeAngle + 0.5f) * eyeDistance * 0.5f,
						centerY + (float) Math.sin(eyeAngle + 0.5f) * eyeDistance * 0.5f,
						eyeX, eyeY);
				canvas.drawPath(neuralPath, paint2);
			}

			// Dibujar simetría biológica - patrones orgánicos
			for (int i = 0; i < 8; i++) {
				float patternAngle = (i * (float) Math.PI / 4) + rotationAngle;
				float patternRadius = var_12;
				float px = centerX + (float) Math.cos(patternAngle) * patternRadius;
				float py = centerY + (float) Math.sin(patternAngle) * patternRadius;

				// Puntos bioluminiscentes
				float glowSize = var_1 + (float) Math.sin(animationTime * 4 + i) * var_1;
				paint3.setAlpha(150 + (int) ((float) Math.sin(animationTime * 3 + i) * 100));
				canvas.drawCircle(px, py, glowSize, paint3);
			}
			paint3.setAlpha(255);

			// Indicadores de vida - glóbulos biológicos
			for (int i = 0; i < life; i++) {
				float indicatorX = x + var_5 + (i * var_3);
				float indicatorY = y + h - var_3;
				float pulse = var_1 + (float) Math.sin(animationTime * 8 + i) * var_1;
				canvas.drawCircle(indicatorX, indicatorY, pulse, paint3);
				canvas.drawCircle(indicatorX, indicatorY, var_1, paint5);
			}
		}
	}

	// Enemigos básicos del juego - Versión optimizada
	private class fire2 extends UnidadBase {
		int life = 5;
		public float x, y;
		private Paint paint, paint2, paint3;
		float speed = 1;
		float w, h;
		float round;
		float animationTime = 0;

		float var_5, var_10, var_15, var_20, var_1, var_2, var_3;

		fire2(float x, float y) {
			this.x = x;
			this.y = y;
			w = basesize * 25;
			h = basesize * 25;
			round = basesize * 5;

			// Paint principal - cuerpo simplificado
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(0xff4a148c);

			// Paint secundario - bordes
			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setStyle(Paint.Style.STROKE);
			paint2.setStrokeWidth(2);
			paint2.setColor(0xff9c27b0);

			// Paint para núcleo
			paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint3.setStyle(Paint.Style.FILL);
			paint3.setColor(0xff00e676);

			var_1 = basesize * 1;
			var_2 = basesize * 2;
			var_3 = basesize * 3;
			var_5 = basesize * 5;
			var_10 = basesize * 10;
			var_15 = basesize * 15;
			var_20 = basesize * 20;
		}

		public void start(Canvas canvas) {
			animationTime += 0.05f;

			// Simplificar actualización de color
			int[] colors = { 0xff4a148c, 0xff6a1b9a, 0xff7b1fa2, 0xff8e24aa, 0xff9c27b0 };
			paint.setColor(life > 0 && life <= colors.length ? colors[life - 1] : colors[0]);

			y = y + speed * basesize;

			// Cuerpo simple - ovalado
			RectF body = new RectF(x + var_5, y + var_5, x + w - var_5, y + h - var_5);
			canvas.drawOval(body, paint);
			canvas.drawOval(body, paint2);

			// Núcleo simple
			float corePulse = var_5 + (float) Math.sin(animationTime * 3) * var_2;
			canvas.drawCircle(x + w / 2, y + h / 2, corePulse, paint3);

			// Indicadores simples
			for (int i = 0; i < life; i++) {
				float indicatorX = x + var_5 + (i * var_3);
				float indicatorY = y + h - var_3;
				canvas.drawCircle(indicatorX, indicatorY, var_1, paint3);
			}
		}
	}

	private class Block extends UnidadBase {
		int life = 5;
		public float x, y;
		private Paint paint;
		Paint paint2;
		float speed = 1;
		float w, h;
		float round;

		float var_5, var_10;

		Block(float x, float y) {
			this.x = x;
			this.y = y;
			w = basesize * 25;
			h = basesize * 25;
			round = basesize * 5;
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(Color.RED);

			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setColor(0x90202020);

			var_5 = basesize * 5;
			var_10 = basesize * 10;
		}

		public void start(Canvas canvas) {

			// Color del bloque según vida (enemigos sí cambian de color)
			paint.setColor(getColorLevel(life > 0 ? life - 1 : 0));
			y = y + speed;
			RectF rectf = new RectF(x, y, x + w, y + h);
			RectF rectf1 = new RectF(x + var_5, y + var_5, x + var_10, y + var_10);
			RectF rectf2 = new RectF(x + w - var_10, y + var_5, x + w - var_5, y + var_10);
			RectF rectf3 = new RectF(x + var_5, y + h - var_10, x + w - var_5, y + h - var_5);

			canvas.drawRoundRect(rectf, round, round, paint);
			canvas.drawRect(rectf1, paint2);
			canvas.drawRect(rectf2, paint2);
			canvas.drawRect(rectf3, paint2);
		}
	}

	public static float getDip(Context _context, int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input,
				_context.getResources().getDisplayMetrics());
	}

	public static int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}

	public class Nave extends UnidadBase {
		float x = 0, y = 0;
		int speedAtak = 25;
		int life = 5; // Vida máxima limitada a 5
		final int MAX_LIFE = 5; // Constante para vida máxima
		Paint paint, paint2, paint3, paint4, paint5;
		Bitmap bitmap;
		float animationTime = 0;
		float engineGlow = 0;
		float cannonPosition = 0; // Posición en el carril inferior
		float cannonSpeed = 2f; // Velocidad de movimiento automático
		float cannonDirection = 1; // Dirección de movimiento (1 = derecha, -1 = izquierda)
		float screenCenterX; // Centro de la pantalla para límites
		float cannonRange; // Rango de movimiento del cañón

		Nave() {
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(0xff0080ff); // Azul brillante
			paint.setStyle(Paint.Style.FILL);

			// Paint para detalles y bordes
			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setColor(0xff00ffff); // Cyan brillante
			paint2.setStyle(Paint.Style.STROKE);
			paint2.setStrokeWidth(n != null ? n.get(2) : 2);

			// Paint para motores
			paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint3.setColor(0xffff6600); // Naranja brillante
			paint3.setStyle(Paint.Style.FILL);
			paint3.setShadowLayer(8 * basesize, 0, 0, 0xffff0000);

			// Paint para cabina
			paint4 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint4.setColor(0xffffff00); // Amarillo brillante
			paint4.setStyle(Paint.Style.FILL);
			paint4.setAlpha(200);

			// Paint para escudo/deflectores
			paint5 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint5.setColor(0xff00ff00); // Verde brillante
			paint5.setStyle(Paint.Style.STROKE);
			paint5.setStrokeWidth(n != null ? n.get(1) : 1);

			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nave_small);
		}

		public void draw(Canvas canvas, float x, float y) {
			this.x = x;
			this.y = y;
			drawBitmapCenter(canvas, bitmap, basesize, x, y, paint);
		}

		public void draw2(Canvas canvas, float screenWidth, float screenHeight) {
			// Inicializar valores de pantalla si es la primera vez
			if (screenCenterX == 0) {
				screenCenterX = screenWidth / 2;
				cannonRange = screenWidth * 0.4f; // El cañón se mueve en 40% del ancho
				this.x = screenCenterX; // Posición inicial en el centro
			}

			// Limitar movimiento del cañón al carril inferior
			// La posición X ya viene del control táctil (event_move)
			// Solo aseguramos que no se salga de los límites de la pantalla
			this.x = Math.max(n.n20, Math.min(screenWidth - n.n20, this.x));

			// Posición fija en la parte inferior (80% desde arriba)
			this.y = screenHeight * 0.8f;

			animationTime += 0.03f;
			engineGlow = 0.5f + (float) Math.sin(animationTime * 4) * 0.5f;

			// Dibujar carril/rail del cañón
			paint2.setAlpha(100);
			canvas.drawLine(screenCenterX - cannonRange, this.y, screenCenterX + cannonRange, this.y, paint2);
			paint2.setAlpha(255);

			// Dibujar escudo energético
			int shieldAlpha = 100 + (int) ((float) Math.sin(animationTime * 2) * 50);
			paint5.setAlpha(shieldAlpha);
			canvas.drawCircle(this.x, this.y, n.n30, paint5);
			paint5.setAlpha(255);

			// Dibujar cuerpo principal - cañón de muro mejorado
			Path cannonBody = new Path();

			// Base del cañón (forma rectangular con bordes redondeados)
			cannonBody.moveTo(this.x - n.n20, this.y - n.n8);
			cannonBody.lineTo(this.x - n.n20, this.y + n.n8);
			cannonBody.lineTo(this.x - n.n15, this.y + n.n10);
			cannonBody.lineTo(this.x + n.n15, this.y + n.n10);
			cannonBody.lineTo(this.x + n.n20, this.y + n.n8);
			cannonBody.lineTo(this.x + n.n20, this.y - n.n8);
			cannonBody.lineTo(this.x + n.n15, this.y - n.n10);
			cannonBody.lineTo(this.x - n.n15, this.y - n.n10);
			cannonBody.close();

			// Color fijo para el cañón (ya no cambia según vida)
			paint.setColor(0xff0080ff); // Azul brillante constante
			canvas.drawPath(cannonBody, paint);
			canvas.drawPath(cannonBody, paint2);

			// Dibujar cañón principal (tubo)
			RectF cannonTube = new RectF(this.x - n.n4, this.y - n.n15, this.x + n.n4, this.y - n.n5);
			canvas.drawRoundRect(cannonTube, n.n2, n.n2, paint);
			canvas.drawRoundRect(cannonTube, n.n2, n.n2, paint2);

			// Dibujar cabina/sensor
			canvas.drawCircle(this.x, this.y - n.n5, n.n4, paint4);
			canvas.drawCircle(this.x, this.y - n.n5, n.n2, paint);

			// Dibujar motores con efecto de pulsación
			float engineSize = n.n3 * (1 + engineGlow * 0.3f);

			// Motor izquierdo
			canvas.drawCircle(this.x - n.n8, this.y + n.n10, engineSize, paint3);
			canvas.drawCircle(this.x - n.n8, this.y + n.n10, n.n2, paint);

			// Motor derecho
			canvas.drawCircle(this.x + n.n8, this.y + n.n10, engineSize, paint3);
			canvas.drawCircle(this.x + n.n8, this.y + n.n10, n.n2, paint);

			// Motor central
			canvas.drawCircle(this.x, this.y + n.n8, engineSize * 0.8f, paint3);
			canvas.drawCircle(this.x, this.y + n.n8, n.n2, paint);

			// Dibujar llamas de los motores
			for (int i = 0; i < 3; i++) {
				float flameOffset = (float) Math.sin(animationTime * 8 + i * 2) * n.n2;
				float flameX = this.x + (i - 1) * n.n8;
				float flameY = this.y + n.n15 + flameOffset;

				Path flame = new Path();
				flame.moveTo(flameX - n.n1, flameY);
				flame.lineTo(flameX, flameY + n.n8 + engineGlow * n.n4);
				flame.lineTo(flameX + n.n1, flameY);
				flame.close();

				paint3.setAlpha(150 + (int) (engineGlow * 100));
				canvas.drawPath(flame, paint3);
				paint3.setAlpha(255);
			}

			// Dibujar armas laterales
			canvas.drawRect(this.x - n.n20, this.y - n.n2, this.x - n.n15, this.y + n.n2, paint2);
			canvas.drawRect(this.x + n.n15, this.y - n.n2, this.x + n.n20, this.y + n.n2, paint2);

			// Dibujar puntos de energía giratorios
			for (int i = 0; i < 6; i++) {
				float angle = -animationTime + (i * (float) Math.PI / 3);
				float px = this.x + (float) Math.cos(angle) * n.n12;
				float py = this.y + (float) Math.sin(angle) * n.n12;
				canvas.drawCircle(px, py, n.n1, paint4);
			}

			// Dibujar sensores frontales
			canvas.drawCircle(this.x - n.n5, this.y - n.n12, n.n1, paint2);
			canvas.drawCircle(this.x + n.n5, this.y - n.n12, n.n1, paint2);
		}

		public void life(int l) {
			life = life + l;
			// Limitar vida al máximo y mínimo permitidos
			if (life > MAX_LIFE)
				life = MAX_LIFE;
			if (life < 0)
				life = 0;

			// Ya no se cambia el color según vida, se mantiene azul constante
		}

		public Fire fire() {
			return new Fire(x, y);
		}
	}

	public static class UnidadBase {
		private boolean isEliminada = false;

		public boolean isEliminada() {
			return isEliminada;
		}

		public void eliminar() {
			isEliminada = true;
		}
	}

	public class ItemTriple extends Item {
		float x, y;
		Paint paint1, paint2, paint3, paint4;
		float giro = 0;
		float animationTime = 0;
		float v5, v8, v10, v12, v15, v18, v20, v25, v1, v2, v3, v4;

		ItemTriple(float x, float y) {
			super(5000);
			setTag("triple");
			this.x = x;
			this.y = y;

			// Paint principal - hexágono exterior
			paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint1.setStyle(Paint.Style.STROKE);
			paint1.setStrokeWidth(n != null ? n.get(3) : 3);
			paint1.setColor(0xff00ffff); // Cyan brillante
			paint1.setShadowLayer(8 * basesize, 0, 0, 0xff0088ff);

			// Paint secundario - núcleo energético
			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setStyle(Paint.Style.FILL);
			paint2.setColor(0xff0080ff); // Azul eléctrico

			// Paint para proyectiles
			paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint3.setStyle(Paint.Style.FILL);
			paint3.setColor(0xffff6600); // Naranja energético
			paint3.setShadowLayer(5 * basesize, 0, 0, 0xffff0000);

			// Paint para detalles
			paint4 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint4.setStyle(Paint.Style.STROKE);
			paint4.setStrokeWidth(n != null ? n.get(1) : 1);
			paint4.setColor(0xffffff00); // Amarillo brillante

			v1 = basesize * 1;
			v2 = basesize * 2;
			v3 = basesize * 3;
			v4 = basesize * 4;
			v5 = basesize * 5;
			v8 = basesize * 8;
			v10 = basesize * 10;
			v12 = basesize * 12;
			v15 = basesize * 15;
			v18 = basesize * 18;
			v20 = basesize * 20;
			v25 = basesize * 25;
		}

		public void draw(Canvas canvas) {
			y = y + basesize * 2;
			giro += 0.03f;
			animationTime += 0.05f;

			// Dibujar hexágono exterior rotante
			canvas.save();
			canvas.translate(x, y);
			canvas.rotate(giro * 180 / (float) Math.PI);

			Path hexagon = new Path();
			for (int i = 0; i < 6; i++) {
				float angle = i * (float) Math.PI / 3;
				float px = (float) Math.cos(angle) * v20;
				float py = (float) Math.sin(angle) * v20;
				if (i == 0) {
					hexagon.moveTo(px, py);
				} else {
					hexagon.lineTo(px, py);
				}
			}
			hexagon.close();

			canvas.drawPath(hexagon, paint1);
			canvas.restore();

			// Dibujar núcleo central pulsante
			float corePulse = v8 + (float) Math.sin(animationTime * 3) * v2;
			canvas.drawCircle(x, y, corePulse, paint2);
			canvas.drawCircle(x, y, v4, paint4);

			// Dibujar tres proyectiles en formación triangular
			for (int i = 0; i < 3; i++) {
				float angle = -giro + (i * (float) Math.PI * 2 / 3);
				float orbitRadius = v12;
				float px = x + (float) Math.cos(angle) * orbitRadius;
				float py = y + (float) Math.sin(angle) * orbitRadius;

				// Proyectil con efecto de energía
				float projectileSize = v4 + (float) Math.sin(animationTime * 8 + i) * v1;
				canvas.drawCircle(px, py, projectileSize, paint3);
				canvas.drawCircle(px, py, v2, paint2);

				// Línea de energía hacia el centro
				canvas.drawLine(x, y, px, py, paint4);
			}

			// Dibujar anillos energéticos concéntricos
			for (int i = 0; i < 3; i++) {
				float ringRadius = v15 + i * v3;
				int alpha = 100 - i * 30;
				paint1.setAlpha(alpha);
				canvas.drawCircle(x, y, ringRadius, paint1);
			}
			paint1.setAlpha(255);

			// Dibujar partículas de energía
			for (int i = 0; i < 8; i++) {
				float angle = animationTime * 2 + (i * (float) Math.PI / 4);
				float px = x + (float) Math.cos(angle) * (v18 + (float) Math.sin(animationTime * 4 + i) * v3);
				float py = y + (float) Math.sin(angle) * (v18 + (float) Math.sin(animationTime * 4 + i) * v3);
				canvas.drawCircle(px, py, v1, paint3);
			}
		}
	}

	public class ItemSpeed extends UnidadBase {
		float x, y;
		Paint paint1, paint2, paint3;
		float v2, v5, v8, v10, v15, v20;
		float rotation = 0;

		ItemSpeed(float x, float y) {
			this.x = x;
			this.y = y;

			// Paint principal - flechas de velocidad
			paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint1.setStyle(Paint.Style.FILL);
			paint1.setColor(0xff00ffff);
			paint1.setShadowLayer(8 * basesize, 0, 0, 0xff00ffff);

			// Paint secundario - contorno brillante
			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setStyle(Paint.Style.STROKE);
			paint2.setStrokeWidth(basesize * 2);
			paint2.setColor(0xff0088ff);

			// Paint central - núcleo energético
			paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint3.setStyle(Paint.Style.FILL);
			paint3.setColor(0xffffffff);
			paint3.setAlpha(200);

			v2 = basesize * 2;
			v5 = basesize * 5;
			v8 = basesize * 8;
			v10 = basesize * 10;
			v15 = basesize * 15;
			v20 = basesize * 20;
		}

		public void draw(Canvas canvas) {
			y = y + 2;
			rotation += 0.1f;

			canvas.save();
			canvas.translate(x, y);
			canvas.rotate(rotation * 180 / (float) Math.PI);

			// Dibujar núcleo central brillante
			canvas.drawCircle(0, 0, v8, paint3);

			// Dibujar flechas de velocidad en 4 direcciones
			for (int i = 0; i < 4; i++) {
				canvas.save();
				canvas.rotate(i * 90);

				// Flecha principal
				Path arrow = new Path();
				arrow.moveTo(0, -v20);
				arrow.lineTo(-v5, -v10);
				arrow.lineTo(-v2, -v10);
				arrow.lineTo(-v2, v5);
				arrow.lineTo(v2, v5);
				arrow.lineTo(v2, -v10);
				arrow.lineTo(v5, -v10);
				arrow.close();

				canvas.drawPath(arrow, paint1);
				canvas.drawPath(arrow, paint2);
				canvas.restore();
			}

			// Dibujar anillo exterior pulsante
			float pulse = v15 + (float) Math.sin(rotation * 3) * 3;
			canvas.drawCircle(0, 0, pulse, paint2);

			canvas.restore();
		}
	}

	public static boolean isClick(float X, float Y, float x, float y, float radio) {
		float rx = X - x;
		float ry = Y - y;
		return java.lang.Math.hypot(rx, ry) <= radio;
	}

	// Método mejorado para detección de colisión entre proyectil y item nuclear
	public static boolean isCollisionFireNuclear(Fire fire, ItemNuclear nuclear) {
		// Usar un radio más grande y preciso para el item nuclear
		float collisionRadius = 20 * basesize; // Radio aumentado para mejor detección

		// Calcular distancia entre centros
		float dx = fire.x - nuclear.x;
		float dy = fire.y - nuclear.y;
		float distance = (float) Math.sqrt(dx * dx + dy * dy);

		return distance <= collisionRadius;
	}

	public class TextFade extends UnidadBase {
		String texto;
		float x, y;
		float fade = 255;
		Paint paint;
		int color;

		public TextFade(String texto, float x, float y, int color) {
			this.texto = texto;
			this.color = color;
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			this.x = x;
			this.y = y;
			paint.setColor(color);
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setTypeface(paintText.getTypeface());
			paint.setTextSize(basesize * 20);
		}

		public void draw(Canvas canvas) {
			fade = fade - 2.5f;
			paint.setAlpha((int) fade);
			y = y - 0.25f;
			canvas.drawText(texto, x, y, paint);
			if (fade == 0) {
				eliminar();
			}
		}
	}

	public static int getColorLevel(int index) {
		int[] colors = {
				0xffff0000,
				0xffff8000,
				0xffffff00,
				0xff80ff00,
				0xff00ff00,
				0xff00c000
		};
		if (index >= colors.length) {
			return 0xffffffff;
		}
		return colors[index];
	}

	public float getNivelSpeed() {
		float n[] = { 1, 1.5f, 3, 4, 5, 5.5f, 6.5f, 7 };
		return n[nivel];
	}

	/*
	 * public class Enemigo extends UnidadBase{
	 * float x=0,y=0;
	 * float x1=1,y1=0.30f;
	 * float speed = 2;
	 * 
	 * void draw(Canvas canvas){
	 * float w = getWidth();
	 * float h = getHeight();
	 * float var_5=basesize*5;
	 * if(x>w||x<0){
	 * x1=-x1;
	 * }
	 * 
	 * if(y>h||y<0){
	 * y1=-y1;
	 * }
	 * x=x+(x1*speed);
	 * y=y+(y1*speed);
	 * 
	 * canvas.drawCircle(x,y,var_5,paintText);
	 * }
	 * 
	 * }
	 */

	public class Muro extends UnidadBase {
		Paint paint;
		Paint paint1;
		int interval = 0;
		float height = 0;
		int life = 5; // Vida máxima limitada a 5
		final int MAX_LIFE = 5; // Constante para vida máxima

		Muro() {
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(0xff0000ff);
			paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint1.setColor(0xf0ffffff);
			height = basesize * 32;

		}

		void draw(Canvas canvas) {
			float w = getWidth();
			float h = getHeight();

			float var_16 = basesize * 16;
			float var_32 = basesize * 32;

			// Limitar vida al máximo permitido
			if (life > MAX_LIFE)
				life = MAX_LIFE;
			if (life < 0)
				life = 0;
			float var_8 = basesize * 8;

			RectF rectf = new RectF(0, h - var_32, w, h);
			canvas.drawRect(rectf, paint);
			canvas.drawLine(0, h - var_32, w, h - var_32, paint1);
			canvas.drawLine(0, h - var_16, w, h - var_16, paint1);
			canvas.drawLine(0, h, w, h, paint1);

			if (++interval == 1000) {
				interval = 0;
			}
			if (interval == 500) {
				textfades
						.add(new TextFade("Bombas fuera", (float) getWidth() / 2, (float) getHeight() / 2, 0xff0000ff));
			}
			for (float i = 0; i < getWidth() + var_16; i = i + var_16) {
				canvas.drawLine(i, h - var_16, i, h, paint1);
				canvas.drawLine(i - var_8, h - var_32, i - var_8, h - var_16, paint1);

				if (interval == 500) {
					if (isEliminada()) {
						return;
					}
					Fire fire = new Fire(i, getHeight());
					fire.paint.setColor(paint.getColor());
					fires.add(fire);

				}
			}

		}
	}

	public static Paint newPaint() {
		return new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	public class Asteroide extends UnidadBase {
		float x, y;
		Bitmap bitmap;
		Paint paint;
		float speed = 4;
		float radio;
		int w, h;

		Asteroide(float x, float y) {
			this.x = x;
			this.y = y;
			bitmap = getBitmapRotate(BitmapFactory.decodeResource(getResources(), R.drawable.fire), 180);
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);

			w = bitmap.getWidth();
			h = bitmap.getHeight();
			radio = Math.max(w, h) / 2;
			speed = speed * basesize;

		}

		void draw(Canvas canvas) {
			y = y + speed;

			canvas.drawBitmap(bitmap, x - (w / 2), y - (h / 2), paint);
		}

		public void setSpeed(float speed) {
			this.speed = speed * basesize;
		}
	}

	public static Bitmap getBitmapRotate(final Bitmap src, float angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		Bitmap bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);

		return bitmap;
	}

	public class Explocion2 extends UnidadBase {
		float x, y;
		ImageFrame img;
		int max = 14;
		int min = 0;

		public Explocion2(float x, float y) {
			this.x = x - 24.5f;
			this.y = y - 24.5f;

			img = new ImageFrame(BitmapFactory.decodeResource(getResources(), R.drawable.explode_big), this.x, this.y);
			img.prepare(14, 1);
		}

		void draw(Canvas canvas) {
			img.draw(canvas);
			++min;
			if (min == max) {
				eliminar();
			}
		}
	}

	public static class ImageFrame {
		Bitmap image;
		float x, y;
		int w, h;

		int filas = 1;
		int columnas = 1;

		int indexFila = 1;
		int indexColumna = 1;

		int numColumnas = 1;
		int numFilas = 1;

		public ImageFrame(Bitmap image, float x, float y) {
			this.image = image;
			this.x = x;
			this.y = y;
		}

		public void cutImage(int x, int y, int w, int h) {
			image = Bitmap.createBitmap(image, x, y, w, h);
		}

		public void prepare(int numC, int numF) {
			columnas = numC;
			filas = numF;

			w = image.getWidth() / columnas;
			h = image.getHeight() / filas;

			numColumnas = image.getWidth() / w;
			numFilas = image.getHeight() / h;
		}

		public void draw(Canvas canvas) {

			int indexX = (w * indexColumna) - w;
			int indexY = (h * indexFila) - h;

			Bitmap frame = Bitmap.createBitmap(image, indexX, indexY, w, h);

			canvas.drawBitmap(frame, x, y, new Paint(Paint.ANTI_ALIAS_FLAG));

			indexColumna = ++indexColumna <= numColumnas ? indexColumna : 1;

		}
	}

	public static boolean isAfecte(float x, float y, float w, float h, float X, float Y) {
		return X > x && Y > y && X < x + w && Y < y + h;
	}

	public class Item extends UnidadBase {
		int duration;
		Object tag;

		public Item(int duration) {
			this.duration = duration;
		}

		public void setDuration(int ms) {
			duration = ms;
		}

		public int getDuration() {
			return duration;
		}

		public void setTag(Object tag) {
			this.tag = tag;
		}

		public Object getTag() {
			return tag;
		}
	}

	void setMultifire() {
		++multiatak;
		getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				--multiatak;
			}
		}, 5000);
	}

	public void toast(String str) {
		Toast.makeText(getContext(), str, Toast.LENGTH_LONG).show();
	}

	public static class Explocion extends UnidadBase {
		float x, y, radio;
		int color;
		Paint paint;

		public Explocion(float x, float y, float radio, int color) {
			this.x = x;
			this.y = y;
			this.radio = radio;
			this.color = color;
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(color);
		}

		float frames = 100;

		public void draw(Canvas canvas) {
			paint.setAlpha((int) (255.0f * (frames / 100)));
			canvas.drawCircle(x, y, radio - (radio * (frames / 100)), paint);
			frames = frames - 5;
			if (frames <= 0) {
				eliminar();
			}
		}
	}

	public class Base extends UnidadBase {
		float x, y;

		Base(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	public class ItemNuclear extends Base {
		Paint paint, paint2, paint3, paint4;
		float animationTime = 0;
		float rotationAngle = 0;
		float pulseSize = 0;

		public ItemNuclear(float x, float y) {
			super(x, y);

			// Paint principal - núcleo radiactivo
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(0xffff0000); // Rojo intenso

			// Paint secundario - anillo de advertencia
			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setStyle(Paint.Style.STROKE);
			paint2.setStrokeWidth(3 * basesize);
			paint2.setColor(0xffffff00); // Amarillo de advertencia

			// Paint para símbolo radiactivo
			paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint3.setStyle(Paint.Style.FILL);
			paint3.setColor(0xff000000); // Negro para símbolo

			// Paint para efectos de energía
			paint4 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint4.setStyle(Paint.Style.FILL);
			paint4.setColor(0xffff6600); // Naranja radiactivo
			paint4.setAlpha(150);
		}

		public void draw(Canvas canvas) {
			// Actualizar animaciones
			animationTime += 0.05f;
			rotationAngle += 2;
			pulseSize = (float) Math.sin(animationTime * 3) * 2 * basesize;

			float centerX = x;
			float centerY = y;
			float baseSize = 15 * basesize;

			// Guardar estado del canvas para rotación
			canvas.save();
			canvas.translate(centerX, centerY);
			canvas.rotate(rotationAngle);
			canvas.translate(-centerX, -centerY);

			// Dibujar aura de energía pulsante
			float auraSize = baseSize + pulseSize;
			paint4.setAlpha(100 + (int) (Math.sin(animationTime * 4) * 50));
			canvas.drawCircle(centerX, centerY, auraSize, paint4);

			// Dibujar círculo exterior (cápsula nuclear)
			canvas.drawCircle(centerX, centerY, baseSize, paint);

			// Dibujar anillo de advertencia
			canvas.drawCircle(centerX, centerY, baseSize - 3 * basesize, paint2);

			// Dibujar símbolo radiactivo (trébol de 3 hojas)
			drawRadioactiveSymbol(canvas, centerX, centerY, baseSize * 0.6f);

			// Dibujar puntos de energía giratorios
			for (int i = 0; i < 6; i++) {
				float angle = (animationTime * 2) + (i * 60) * (float) Math.PI / 180;
				float particleX = centerX + (float) Math.cos(angle) * (baseSize + 5 * basesize);
				float particleY = centerY + (float) Math.sin(angle) * (baseSize + 5 * basesize);

				paint4.setAlpha(200);
				canvas.drawCircle(particleX, particleY, 2 * basesize, paint4);
			}

			// Restaurar estado del canvas
			canvas.restore();

			// Incrementar posición Y para movimiento hacia abajo
			y++;
		}

		private void drawRadioactiveSymbol(Canvas canvas, float centerX, float centerY, float size) {
			// Dibujar círculo central
			canvas.drawCircle(centerX, centerY, size * 0.2f, paint3);

			// Dibujar tres hojas del trébol radiactivo
			for (int i = 0; i < 3; i++) {
				float angle = i * 120 * (float) Math.PI / 180; // 120 grados entre cada hoja

				// Crear forma de hoja
				Path leaf = new Path();
				float leafRadius = size * 0.4f;

				// Puntos para formar una hoja de trébol
				float tipX = centerX + (float) Math.cos(angle) * leafRadius;
				float tipY = centerY + (float) Math.sin(angle) * leafRadius;

				float base1X = centerX + (float) Math.cos(angle - 0.5f) * (leafRadius * 0.3f);
				float base1Y = centerY + (float) Math.sin(angle - 0.5f) * (leafRadius * 0.3f);

				float base2X = centerX + (float) Math.cos(angle + 0.5f) * (leafRadius * 0.3f);
				float base2Y = centerY + (float) Math.sin(angle + 0.5f) * (leafRadius * 0.3f);

				// Dibujar hoja
				leaf.moveTo(centerX, centerY);
				leaf.quadTo(base1X, base1Y, tipX, tipY);
				leaf.quadTo(base2X, base2Y, centerX, centerY);
				leaf.close();

				canvas.drawPath(leaf, paint3);
			}
		}
	}

	public static void drawBitmapCenter(Canvas canvas, Bitmap bitmap, float basesize, float x, float y, Paint paint) {
		float w = bitmap.getWidth() * basesize;
		float h = bitmap.getHeight() * basesize;
		float px = x - (w / 2);
		float py = y - (h / 2);
		canvas.drawBitmap(bitmap, px, py, paint);
	}

	private SoundPool soundpool;
	private Map<Integer, Integer> map_sound = new HashMap<>();

	private void createSoundPool() {
		if (Build.VERSION.SDK_INT > 21) {
			SoundPool.Builder builder = new SoundPool.Builder();
			// El número de audio entrante
			builder.setMaxStreams(5);
			// AudioAttributes es un método para encapsular varios atributos de audio
			AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
			// Establecer los atributos apropiados de la secuencia de audio
			attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC); // Cambiado a STREAM_MUSIC
			// Cargar un AudioAttributes
			builder.setAudioAttributes(attrBuilder.build());
			soundpool = builder.build();
		} else {
			soundpool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0); // Ya estaba en STREAM_MUSIC
		}

		map_sound.put(1, soundpool.load(getContext(), R.raw.sound1, 1));
		map_sound.put(2, soundpool.load(getContext(), R.raw.sound2, 1));
		map_sound.put(3, soundpool.load(getContext(), R.raw.sound3, 1));
		map_sound.put(4, soundpool.load(getContext(), R.raw.sound4, 1));
		map_sound.put(5, soundpool.load(getContext(), R.raw.sound5, 1));
		map_sound.put(6, soundpool.load(getContext(), R.raw.sound6, 1));
		map_sound.put(7, soundpool.load(getContext(), R.raw.sound7, 1));
		map_sound.put(8, soundpool.load(getContext(), R.raw.mission_complete, 1));
		map_sound.put(9, soundpool.load(getContext(), R.raw.mission_1, 1));

	}

	private void startSoundPool(int index) {
		if (soundpool == null) {
			createSoundPool();
		}

		soundpool.play(map_sound.get(index), 1, 1, 0, 0, 1);
	}

	public static class BaseItem {
		public float x;
		public float y;
		Bitmap bitmap;
		Paint paint;
		public int w;
		public int h;
		boolean consumido = false;

		public BaseItem(Bitmap bitmap, float x, float y) {
			this.bitmap = bitmap;
			this.x = x;
			this.y = y;
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);

			// Manejar bitmap null para items personalizados
			if (bitmap != null) {
				w = bitmap.getWidth();
				h = bitmap.getHeight();
			} else {
				// Dimensiones por defecto para items dibujados
				w = (int) (24 * basesize); // Ancho por defecto
				h = (int) (24 * basesize); // Alto por defecto
			}
		}

		public void draw(Canvas canvas) {
			// Solo dibujar bitmap si existe
			if (bitmap != null) {
				canvas.drawBitmap(bitmap, x, y, paint);
			}
			// Si bitmap es null, las clases hijas sobrescriben draw()
		}

		public void move(Canvas canvas, float x, float y) {
			this.x = this.x + x;
			this.y = this.y + y;
			draw(canvas);
		}

		public void consumir() {
			consumido = true;
		}

		public void destroy() {
			consumido = true;
		}

	}

	public class PlusLifeNave extends BaseItem {
		Paint paint, paint2, paint3;
		float animationTime = 0;
		float rotationAngle = 0;

		PlusLifeNave(float x, float y) {
			super(null, x, y); // No usamos bitmap

			// Paint principal - corazón de vida
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(0xff00ff00); // Verde brillante

			// Paint secundario - contorno
			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setStyle(Paint.Style.STROKE);
			paint2.setStrokeWidth(2 * basesize);
			paint2.setColor(Color.WHITE);

			// Paint para efectos
			paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint3.setStyle(Paint.Style.FILL);
			paint3.setColor(0xff00ff00);
			paint3.setAlpha(100);
		}

		@Override
		public void consumir() {
			startSoundPool(2);
			super.consumir();
		}

		public void move(Canvas canvas) {
			if (consumido) {
				return;
			}

			// Actualizar animaciones
			animationTime += 0.1f;
			rotationAngle += 3;

			// Dibujar item de vida personalizado
			drawHealthItem(canvas);

			// Mover hacia abajo
			y += 2;

			if (y > getHeight()) {
				super.destroy();
			}
		}

		private void drawHealthItem(Canvas canvas) {
			float centerX = x;
			float centerY = y;
			float baseSize = 12 * basesize;

			// Guardar estado para rotación
			canvas.save();
			canvas.translate(centerX, centerY);
			canvas.rotate(rotationAngle);
			canvas.translate(-centerX, -centerY);

			// Dibujar aura pulsante
			float pulseSize = (float) Math.sin(animationTime * 3) * 3 * basesize;
			paint3.setAlpha(80 + (int) (Math.sin(animationTime * 4) * 40));
			canvas.drawCircle(centerX, centerY, baseSize + pulseSize, paint3);

			// Dibujar corazón de vida
			drawHeart(canvas, centerX, centerY, baseSize);

			// Dibujar símbolo "+" en el centro
			paint2.setColor(Color.WHITE);
			paint2.setStrokeWidth(3 * basesize);
			float crossSize = baseSize * 0.4f;
			canvas.drawLine(centerX - crossSize, centerY, centerX + crossSize, centerY, paint2);
			canvas.drawLine(centerX, centerY - crossSize, centerX, centerY + crossSize, paint2);

			// Restaurar estado
			canvas.restore();
		}

		private void drawHeart(Canvas canvas, float centerX, float centerY, float size) {
			Path heart = new Path();

			// Crear forma de corazón
			float width = size;
			float height = size * 0.9f;

			// Lado izquierdo del corazón
			heart.moveTo(centerX, centerY + height * 0.3f);
			heart.cubicTo(centerX - width * 0.5f, centerY - height * 0.3f,
					centerX - width * 0.5f, centerY - height * 0.7f,
					centerX, centerY - height * 0.4f);

			// Lado derecho del corazón
			heart.cubicTo(centerX + width * 0.5f, centerY - height * 0.7f,
					centerX + width * 0.5f, centerY - height * 0.3f,
					centerX, centerY + height * 0.3f);

			heart.close();
			canvas.drawPath(heart, paint);
			canvas.drawPath(heart, paint2);
		}
	}

	public class PlusLifeMuro extends BaseItem {
		Paint paint, paint2, paint3;
		float animationTime = 0;
		float rotationAngle = 0;

		PlusLifeMuro(float x, float y) {
			super(null, x, y); // No usamos bitmap

			// Paint principal - escudo de planeta
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(0xff0080ff); // Azul brillante

			// Paint secundario - contorno
			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setStyle(Paint.Style.STROKE);
			paint2.setStrokeWidth(2 * basesize);
			paint2.setColor(Color.WHITE);

			// Paint para efectos
			paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint3.setStyle(Paint.Style.FILL);
			paint3.setColor(0xff0080ff);
			paint3.setAlpha(100);
		}

		@Override
		public void consumir() {
			startSoundPool(2);
			super.consumir();
		}

		public void move(Canvas canvas) {
			if (consumido) {
				return;
			}

			// Actualizar animaciones
			animationTime += 0.1f;
			rotationAngle -= 3; // Rotación en sentido contrario

			// Dibujar item de vida del muro personalizado
			drawPlanetHealthItem(canvas);

			// Mover hacia abajo
			y += 2;

			if (y > getHeight()) {
				super.destroy();
			}
		}

		private void drawPlanetHealthItem(Canvas canvas) {
			float centerX = x;
			float centerY = y;
			float baseSize = 12 * basesize;

			// Guardar estado para rotación
			canvas.save();
			canvas.translate(centerX, centerY);
			canvas.rotate(rotationAngle);
			canvas.translate(-centerX, -centerY);

			// Dibujar aura pulsante
			float pulseSize = (float) Math.sin(animationTime * 3) * 3 * basesize;
			paint3.setAlpha(80 + (int) (Math.sin(animationTime * 4) * 40));
			canvas.drawCircle(centerX, centerY, baseSize + pulseSize, paint3);

			// Dibujar escudo de planeta
			drawPlanetShield(canvas, centerX, centerY, baseSize);

			// Dibujar símbolo "+" en el centro
			paint2.setColor(Color.WHITE);
			paint2.setStrokeWidth(3 * basesize);
			float crossSize = baseSize * 0.4f;
			canvas.drawLine(centerX - crossSize, centerY, centerX + crossSize, centerY, paint2);
			canvas.drawLine(centerX, centerY - crossSize, centerX, centerY + crossSize, paint2);

			// Restaurar estado
			canvas.restore();
		}

		private void drawPlanetShield(Canvas canvas, float centerX, float centerY, float size) {
			// Dibujar círculo principal (planeta)
			canvas.drawCircle(centerX, centerY, size, paint);
			canvas.drawCircle(centerX, centerY, size, paint2);

			// Dibujar anillo de protección
			float ringWidth = size * 0.3f;
			RectF ringOuter = new RectF(centerX - size - ringWidth, centerY - size - ringWidth,
					centerX + size + ringWidth, centerY + size + ringWidth);
			RectF ringInner = new RectF(centerX - size + ringWidth / 2, centerY - size + ringWidth / 2,
					centerX + size - ringWidth / 2, centerY + size - ringWidth / 2);

			// Crear forma de anillo
			Path ringPath = new Path();
			ringPath.addOval(ringOuter, Path.Direction.CW);
			ringPath.addOval(ringInner, Path.Direction.CCW);

			paint3.setAlpha(150);
			canvas.drawPath(ringPath, paint3);

			// Dibujar continentes (formas irregulares)
			paint.setColor(0xff004080); // Azul más oscuro para continentes
			canvas.drawCircle(centerX - size * 0.3f, centerY - size * 0.2f, size * 0.25f, paint);
			canvas.drawCircle(centerX + size * 0.4f, centerY + size * 0.3f, size * 0.2f, paint);
			canvas.drawCircle(centerX + size * 0.1f, centerY - size * 0.4f, size * 0.15f, paint);

			// Restaurar color principal
			paint.setColor(0xff0080ff);
		}
	}

	public static class Texto {
		float x;
		float y;
		String texto;
		Paint paint1;
		Paint paint2;

		Texto(float x, float y) {
			this.x = x;
			this.y = y;
			paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setStyle(Paint.Style.STROKE);
			paint1.setTextAlign(Paint.Align.CENTER);
			paint2.setTextAlign(Paint.Align.CENTER);
			setTextColor(0xff000000);
			setStrokeColor(0xffffffff);
			setStrokeWidth(1);

		}

		Texto(Texto texto) {
			x = texto.x;
			y = texto.y;
			paint1 = new Paint(texto.paint1);
			paint2 = new Paint(texto.paint2);
			this.texto = texto.texto;
		}

		void setTypeface(Typeface typeface) {
			paint1.setTypeface(typeface);
			paint2.setTypeface(typeface);
		}

		void setTextSize(float size) {
			paint1.setTextSize(size);
			paint2.setTextSize(size);
		}

		void setText(String texto) {
			this.texto = texto;
		}

		void setTextColor(int color) {
			paint1.setColor(color);
		}

		void setStrokeWidth(float width) {
			paint2.setStrokeWidth(width);
		}

		void setStrokeColor(int color) {
			paint2.setColor(color);
		}

		void setStroke(int width, int color) {
			setStrokeColor(color);
			setStrokeWidth(width);
		}

		void draw(Canvas canvas) {
			canvas.drawText(texto, x, y, paint1);
			canvas.drawText(texto, x, y, paint2);
		}

		void darw(Canvas canvas, String texto) {
			canvas.drawText(texto, x, y, paint1);
			canvas.drawText(texto, x, y, paint2);
		}

		void darw(Canvas canvas, String texto, float x, float y) {
			canvas.drawText(texto, x, y, paint1);
			canvas.drawText(texto, x, y, paint2);
		}

	}

	public class Enemigo2 extends Base2 {
		RectF rectf;
		Paint paint, paint2, paint3;
		float mx, my;
		float animationTime = 0;
		float rotationSpeed = 0.02f;

		Enemigo2(float x, float y) {
			super(x, y);
			rectf = new RectF(x, y, x + n.n25, y + n.n25);

			// Paint principal - cuerpo
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(0xff9b59b6); // Púrpura elegante
			paint.setStyle(Paint.Style.FILL);

			// Paint secundario - detalles y bordes
			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setColor(0xffe74c3c); // Rojo brillante
			paint2.setStyle(Paint.Style.STROKE);
			paint2.setStrokeWidth(n.get(2));

			// Paint para ojos - brillantes
			paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint3.setColor(0xffffff00); // Amarillo brillante
			paint3.setStyle(Paint.Style.FILL);
			paint3.setShadowLayer(3 * basesize, 0, 0, 0xffff0000);
		}

		void move(Canvas canvas) {
			move(canvas, mx, my);
		}

		public void move(Canvas canvas, float x, float y) {
			this.x = this.x + x;
			this.y = this.y + y;
			draw(canvas);
		}

		public void draw(Canvas canvas) {
			animationTime += rotationSpeed;

			// Actualizar rectángulo del cuerpo
			rectf = new RectF(x, y, x + n.n25, y + n.n25);

			// Dibujar cuerpo principal - diamante rotante
			canvas.save();
			canvas.translate(x + n.n12, y + n.n12);
			canvas.rotate(animationTime * 180 / (float) Math.PI);

			// Forma de diamante
			Path diamond = new Path();
			diamond.moveTo(0, -n.n12);
			diamond.lineTo(n.n8, -n.n4);
			diamond.lineTo(n.n12, 0);
			diamond.lineTo(n.n8, n.n4);
			diamond.lineTo(0, n.n12);
			diamond.lineTo(-n.n8, n.n4);
			diamond.lineTo(-n.n12, 0);
			diamond.lineTo(-n.n8, -n.n4);
			diamond.close();

			canvas.drawPath(diamond, paint);
			canvas.drawPath(diamond, paint2);
			canvas.restore();

			// Dibujar núcleo central pulsante
			float pulse = n.n3 + (float) Math.sin(animationTime * 3) * n.n1;
			canvas.drawCircle(x + n.n12, y + n.n12, pulse, paint3);

			// Dibujar ojos satélite rotantes
			for (int i = 0; i < 4; i++) {
				float angle = -animationTime + (i * (float) Math.PI / 2);
				float orbitRadius = n.n8;
				float px = x + n.n12 + (float) Math.cos(angle) * orbitRadius;
				float py = y + n.n12 + (float) Math.sin(angle) * orbitRadius;

				// Ojo con efecto de parpadeo
				float eyeSize = (float) Math.sin(animationTime * 8 + i) > 0.7f ? n.n1 : n.n2;
				canvas.drawCircle(px, py, eyeSize, paint3);
				canvas.drawCircle(px, py, n.n1, paint);
			}

			// Dibujar anillo energético exterior
			int ringAlpha = 150 + (int) ((float) Math.sin(animationTime * 2) * 50);
			paint2.setAlpha(ringAlpha);
			canvas.drawCircle(x + n.n12, y + n.n12, n.n15, paint2);
			paint2.setAlpha(255);

			// Dibujar pequeños deflectores
			for (int i = 0; i < 6; i++) {
				float angle = animationTime * 2 + (i * (float) Math.PI / 3);
				float px = x + n.n12 + (float) Math.cos(angle) * n.n10;
				float py = y + n.n12 + (float) Math.sin(angle) * n.n10;
				canvas.drawCircle(px, py, n.n1, paint2);
			}
		}
	}

	public class Boss extends Base2 {
		RectF rectf;
		Paint paint, paint2, paint3, paint4;
		float mx, my;
		float animationTime = 0;
		float shieldRadius = 0;

		Boss(float x, float y) {
			super(x, y);
			rectf = new RectF(x, y, x + n.n40, y + n.n40);

			// Paint principal - cuerpo del jefe
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(0xffdc143c); // Rojo oscuro
			paint.setStyle(Paint.Style.FILL);

			// Paint secundario - detalles y bordes
			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setColor(0xffffd700); // Dorado brillante
			paint2.setStyle(Paint.Style.STROKE);
			paint2.setStrokeWidth(n.get(3));

			// Paint para ojos - brillantes
			paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint3.setColor(0xffffff00); // Amarillo brillante
			paint3.setStyle(Paint.Style.FILL);
			paint3.setShadowLayer(5 * basesize, 0, 0, 0xffff0000);

			// Paint para escudo energético
			paint4 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint4.setColor(0xff00ffff); // Cyan brillante
			paint4.setStyle(Paint.Style.STROKE);
			paint4.setStrokeWidth(n.get(2));
			paint4.setAlpha(150);
		}

		void move(Canvas canvas) {
			move(canvas, mx, my);
		}

		public void move(Canvas canvas, float x, float y) {
			this.x = this.x + x;
			this.y = this.y + y;
			draw(canvas);
		}

		public void draw(Canvas canvas) {
			animationTime += 0.05f;
			shieldRadius = n.n30 + (float) Math.sin(animationTime) * n.n5;

			// Actualizar rectángulo del cuerpo
			rectf = new RectF(x - n.n20, y - n.n20, x + n.n20, y + n.n20);

			// Dibujar escudo energético animado
			paint4.setAlpha(100 + (int) ((float) Math.sin(animationTime * 2) * 50));
			canvas.drawCircle(x, y, shieldRadius, paint4);

			// Dibujar cuerpo principal - hexágono
			Path hexagon = new Path();
			for (int i = 0; i < 6; i++) {
				float angle = (float) Math.toRadians(60 * i);
				float px = x + (float) Math.cos(angle) * n.n20;
				float py = y + (float) Math.sin(angle) * n.n20;
				if (i == 0) {
					hexagon.moveTo(px, py);
				} else {
					hexagon.lineTo(px, py);
				}
			}
			hexagon.close();
			canvas.drawPath(hexagon, paint);
			canvas.drawPath(hexagon, paint2);

			// Dibujar núcleo central
			canvas.drawCircle(x, y, n.n8, paint3);
			canvas.drawCircle(x, y, n.n5, paint);

			// Dibujar anillo exterior con ojos rotantes
			for (int i = 0; i < 8; i++) {
				float angle = animationTime + (i * (float) Math.PI / 4);
				float px = x + (float) Math.cos(angle) * n.n15;
				float py = y + (float) Math.sin(angle) * n.n15;
				canvas.drawCircle(px, py, n.n3, paint3);
			}

			// Dibujar cañones laterales
			float cannonOffset = (float) Math.sin(animationTime * 3) * n.n3;
			// Cañón izquierdo
			canvas.drawRect(x - n.n25 - cannonOffset, y - n.n3, x - n.n15 - cannonOffset, y + n.n3, paint);
			canvas.drawRect(x - n.n25 - cannonOffset, y - n.n2, x - n.n15 - cannonOffset, y + n.n2, paint3);

			// Cañón derecho
			canvas.drawRect(x + n.n15 + cannonOffset, y - n.n3, x + n.n25 + cannonOffset, y + n.n3, paint);
			canvas.drawRect(x + n.n15 + cannonOffset, y - n.n2, x + n.n25 + cannonOffset, y + n.n2, paint3);

			// Dibujar ojos brillantes
			float eyeBlink = (float) Math.sin(animationTime * 8) > 0.8f ? n.n2 : n.n4;
			canvas.drawCircle(x - n.n8, y - n.n5, eyeBlink, paint3);
			canvas.drawCircle(x + n.n8, y - n.n5, eyeBlink, paint3);
			canvas.drawCircle(x - n.n8, y - n.n5, n.n2, paint);
			canvas.drawCircle(x + n.n8, y - n.n5, n.n2, paint);
		}
	}

	public static class Base2 {
		float life = 1;
		float x;
		float y;
		boolean anulable = false;

		public Base2() {
		}

		public Base2(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public void anular() {
			anulable = true;
		}
	}

	public class N {
		final float n1, n2, n3, n4, n5, n8, n10, n12, n15, n20, n25, n30, n40;
		float base;

		N(float basesize) {
			base = basesize;
			n1 = get(1);
			n2 = get(2);
			n3 = get(3);
			n4 = get(4);
			n5 = get(5);
			n8 = get(8);
			n10 = get(10);
			n12 = get(12);
			n15 = get(15);
			n20 = get(20);
			n25 = get(25);
			n30 = get(30);
			n40 = get(40);
		}

		float get(float num) {
			if (base == 0)
				return 0;
			return num * base;
		}
	}

	public void setOnGameOverListener(OnGameOverListener ongameover) {
		if (ongameover != null) {
			this.ongameover = ongameover;
		}
	}

	public void setOnGameVictoryListener(OnGameVictoryListener ongamevictory) {
		if (ongamevictory != null) {
			this.ongamevictory = ongamevictory;
		}
	}

	private boolean isCompleteMission1() {
		return puntos >= 50;
	}

	private boolean isCompleteMission2() {
		return puntos >= 100;
	}

	private boolean isCompleteMission3() {
		return puntos >= 150;
	}

	private boolean isCompleteMission4() {
		return count_nave_lifes >= 10;
	}

	private boolean isCompleteMission5() {
		return count_muro_lifes >= 10;
	}

}
