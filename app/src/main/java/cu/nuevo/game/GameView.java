package cu.spaceattack.boss;
import android.view.*;
import android.content.*;
import android.graphics.*;
import java.util.*;
import android.util.*;
import android.media.*;
import android.widget.*;
import android.os.*;

public class GameView extends View
{
	
	
	public static float basesize = 1;
	int speedAtak = 0;
	int puntos = 0;
	private int nivel = 1;
	private float x,y;
	int textColor=0xffff0000;
	Paint paintText;
	Nave nave = new Nave();
	ArrayList<Fire> fires = new ArrayList<>();
	ArrayList<Block> blocks = new ArrayList<>();
	ArrayList<ItemSpeed> itemSpeed = new ArrayList<>();
	ArrayList<TextFade> textfades = new ArrayList<>();
	ArrayList<ItemTriple> itemtriple = new ArrayList<>();
	ArrayList<Asteroide> asteroides = new ArrayList<>();
	ArrayList<Explocion2> explocionList=new ArrayList<>();
	ArrayList<PlusLifeNave> list_pluslife = new ArrayList<>();
	ArrayList<PlusLifeMuro> list_pluslife_muro = new ArrayList<>();
	ArrayList<Enemigo2> list_enemigo2 = new ArrayList<>();
	ArrayList<Boss> list_boss = new ArrayList<>();
	int lastBossScore = 0;
	
	ArrayList<Item> listitem = new ArrayList<>();
	Runnable runnable,runnable2;
	Muro muro;
	N n;
	ArrayList<Explocion>list_explocion=new ArrayList<>();
	ArrayList<ItemNuclear>list_nuclear=new ArrayList<>();
	int multiatak = 0;
	boolean isGameOver = false;
	SharedPreferences gameData;
	private int level=1;
	int record = 0;
	
	RectF rectf_game;
	ArrayList<Fire> tmp_list_fire = new ArrayList<>();
	
	public GameView(Context context){
		super(context);
		init();
	}
	
	public GameView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}
	
	public GameView(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		init();
	}
	
	private void init(){
		gameData=getContext().getSharedPreferences("gameData",Context.MODE_PRIVATE);
		basesize=getResources().getDisplayMetrics().density;
		n=new N(basesize);
		setBackgroundResource(R.drawable.background1);
		paintText=new Paint(Paint.ANTI_ALIAS_FLAG);
		paintText.setTextSize(14*basesize);
		paintText.setColor(textColor);
		paintText.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"pixel_font.ttf"));
		
		record=gameData.getInt("record",0);
		createSoundPool();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		x=w/2;
		y=h/2;
		rectf_game=new RectF(0,0,w,h);
	}

	
	
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		
		
		if(muro==null){
			muro=new Muro();
		}
		muro.draw(canvas);
		generate();
		if(!nave.isEliminada()){
		nave.draw2(canvas,x,y-n.n40);
		}
		
		gameStart(canvas);
		
		// Solo mostrar puntos y record si el juego no ha terminado
		if(muro.life>0){
			canvas.drawText("PUNTOS: "+puntos,25,25,paintText);
			canvas.drawText("RECORD: "+record,25,50,paintText);
		}
		
		if(muro.life>0){
		
		}else{
			
			
			Texto gameover = new Texto(getWidth()/2,getHeight()/2);
			gameover.setText("GAME OVER");
			gameover.setTextSize(getDip(40));
			gameover.setTextColor(Color.RED);
			gameover.setStrokeColor(Color.WHITE);
			gameover.setStrokeWidth(getDip(0.5f));
			gameover.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"pixel_font.ttf"));
			gameover.draw(canvas);
			
			Texto tt = new Texto(getWidth()/2,(getHeight()/2)+getDip(50));
			tt.setText("PUNTOS "+puntos);
			tt.setTextSize(getDip(30));
			tt.setTextColor(0xffffff00);
			tt.setStrokeColor(Color.RED);
			tt.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"pixel_font.ttf"));
			tt.draw(canvas);
			
			if(puntos>record){
				tt.setText("NUEVO RECORD");
				tt.y=tt.y-getDip(100);
				tt.draw(canvas);
			}
			
		}
		if(!isGameOver||explocionList.size()>0){
		invalidate();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		x=event.getX();
		y=event.getY();
		switch(event.getAction()){
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
	
	
	private void event_down(){
	}
	private void event_move(){
	}
	private void event_up(){
	}
	
	private float getDip(float num){
		return num*basesize;
	}
	
	void disparar(){
		if(nave.isEliminada()){return;}
			if(++speedAtak>=nave.speedAtak){
					if(multiatak>0){
					Fire fire1=nave.fire();
					fire1.dx=0.1f;
					fires.add(fire1);
					Fire fire2=nave.fire();
					fire2.dx=-0.1f;
					fires.add(fire2);
					}
					if(multiatak>1){
					Fire fire3=nave.fire();
					fire3.dx=0.2f;
					fires.add(fire3);
					Fire fire4=nave.fire();
					fire4.dx=-0.2f;
					fires.add(fire4);
					}
					if(multiatak>2){
						Fire fire5=nave.fire();
						fire5.dx=0.3f;
						fires.add(fire5);
						Fire fire6=nave.fire();
						fire6.dx=-0.3f;
						fires.add(fire6);
					}
				
				fires.add(nave.fire());
				speedAtak=0;

			}
	}
	
	
	
	
	int interval_block;
	int interval_fire;
	int interval_ene;
	public void generate(){
		if(isGameOver){return;}
		++ interval_block;
		if(++interval_fire==50){
			interval_fire=0;
			Asteroide ast = new Asteroide(getRandom(50,getWidth()-50),0);
			ast.speed=getRandom(4,6);
			asteroides.add(ast);
		}
		if(interval_block==75){
			interval_block=0;

			Block block = new Block(getRandom(50,getWidth()-50),0);
			block.life=getRandom(1,level);
			block.speed=getRandom(1,nivel);
			blocks.add(block);
			
		}
		if(puntos>100){
		if(++interval_ene==200){
			interval_ene=0;
			Enemigo2 ene= new Enemigo2(getRandom(50,getWidth()-50),0);
			ene.life=3;
			list_enemigo2.add(ene);
		}
		}
		
		// Jefe cada 50 puntos
		if(puntos >= 50 && (puntos - lastBossScore) >= 50){
			Boss jefe = new Boss(getWidth()/2, -50);
			jefe.life = 10; // Jefe más resistente
			list_boss.add(jefe);
			lastBossScore = puntos;
			textfades.add(new TextFade("¡JEFE!", (float)getWidth()/2, (float)getHeight()/2, 0xffff0000));
		}
		disparar();

	}
	
	
	
	
	public void gameStart(Canvas canvas){
		
		for(Explocion explocion:list_explocion){
			explocion.draw(canvas);
		}
		
		for(Fire fire:fires){
			fire.draw(canvas);
		}
		
		for(Block block:blocks){
			block.start(canvas);
		}
		
		for(Enemigo2 ene :list_enemigo2){
			ene.move(canvas,0,1);
			if(ene.y>getHeight()){
				ene.anular();
				startSoundPool(2);
			}
			// Jefe dispara (life=10)
			if(ene.life == 10 && Math.random() < 0.02){
				Fire fire = new Fire(ene.x + n.n12, ene.y + n.n12);
				fire.dy = 2;
				fire.setColor(0xffff0000);
				fires.add(fire);
			}
		}

		for(Boss jefe:list_boss){
			jefe.move(canvas,0,1);
			if(jefe.y>getHeight()){
				jefe.anular();
				startSoundPool(2);
			}
			// Jefe dispara
			if(jefe.life == 10 && Math.random() < 0.02){
				Fire fire = new Fire(jefe.x + n.n12, jefe.y + n.n12);
				fire.dy = 2;
				fire.setColor(0xffff0000);
				fires.add(fire);
			}
		}
		
		for(ItemTriple it :itemtriple){
			it.draw(canvas);
			
			if(isClick(nave.x,nave.y,it.x,it.y,20)){
				it.eliminar();
				textfades.add(new TextFade("Triple fuego",(float)getWidth()/2,(float)getHeight()/2,0xff00ff00));
				setMultifire();
				//startSound(R.raw.sound2);
				startSoundPool(2);
			}
			
			
			if(it.x>getHeight()){it.eliminar();}
		}
		
		
		for(ItemSpeed is :itemSpeed){
			is.draw(canvas);
			if(isClick(nave.x,nave.y,is.x,is.y,20)){
				//startSound(R.raw.sound2);
				startSoundPool(2);
				is.eliminar();
				if(nave.speedAtak>15){
					--nave.speedAtak;
					textfades.add(new TextFade("+ speed atak",(float)getWidth()/2,(float)getHeight()/2,0xff00ff00));
				}else{
					if(runnable==null){
					final int speed = nave.speedAtak;
					nave.speedAtak=5;
					runnable = new Runnable(){

							@Override
							public void run()
							{
								nave.speedAtak=speed;
								runnable=null;
							}
						};
						getHandler().postDelayed(runnable,3000);
						}else{
							getHandler().removeCallbacks(runnable);
							getHandler().postDelayed(runnable,3000);
						}
						
					textfades.add(new TextFade("IPER-SPEED",(float)getWidth()/2,(float)getHeight()/2,0xff00ff00));
						
				}
			}
			if(is.y>getHeight()){
				is.eliminar();
			}
		}
		
		for(TextFade textfade:textfades){
			textfade.draw(canvas);
		}
		
		
		for(Fire fire :fires){
			if(!rectf_game.contains(fire.x,fire.y)){
				fire.eliminar();
			}else{
				for(Enemigo2 ene:list_enemigo2){
					if(!ene.anulable){
					if(ene.rectf.contains(fire.x,fire.y)){
						--ene.life;
						fire.eliminar();
						if(ene.life<=0){
						ene.anular();
						// Jefe da 50 puntos (inicialmente life=10)
						int puntosGanados = (ene.life == 9) ? 50 : 10;
						puntos += puntosGanados;
						textfades.add(new TextFade("+"+puntosGanados,(float)ene.x,(float)ene.y,0xff00ff00));
						list_explocion.add(new Explocion(fire.x,fire.y,basesize*25,0xffff00ff));
						startSoundPool(6);
						blocks.add(new Block(ene.x,ene.y));
						}
					}}
				}
				// Colisiones con jefes
				for(Boss jefe:list_boss){
					if(!jefe.anulable){
					if(jefe.rectf.contains(fire.x,fire.y)){
						--jefe.life;
						fire.eliminar();
						if(jefe.life<=0){
						jefe.anular();
						// Jefe da 50 puntos
						int puntosGanados = 50;
						puntos += puntosGanados;
						textfades.add(new TextFade("+"+puntosGanados,(float)jefe.x,(float)jefe.y,0xff00ff00));
						list_explocion.add(new Explocion(fire.x,fire.y,basesize*25,0xffff00ff));
						startSoundPool(6);
						blocks.add(new Block(jefe.x,jefe.y));
						}
					}}
				}
				for(Block block:blocks){
					if(!block.isEliminada()){
					if(isAfecte(block.x,block.y,block.w,block.h,fire.x,fire.y)){
						fire.eliminar();
						if(--block.life==0){
							
							switch(++puntos){
								case 25:
									level=2;
									break;
								case 50:
									nivel=2;
									//startSound(R.raw.sound5);
									startSoundPool(5);
									setTextFadeCenter("NIVEL "+nivel,getColorLevel(nivel));
									break;
								case 75:
									level=3;
									break;
								case 100:
									nivel=3;
									//startSound(R.raw.sound5);
									startSoundPool(5);
									setTextFadeCenter("NIVEL "+nivel,getColorLevel(nivel));
									break;
								case 125:
									level=4;
									break;
								case 150:
									nivel=4;
									//startSound(R.raw.sound5);
									startSoundPool(5);
									setTextFadeCenter("NIVEL "+nivel,getColorLevel(nivel));
									break;
								case 175:
									level=5;
									break;
								case 200:
									nivel=5;
									//startSound(R.raw.sound5);
									startSoundPool(5);
									setTextFadeCenter("NIVEL "+nivel,getColorLevel(nivel));
									break;
									
							}
							block.eliminar();
							list_explocion.add(new Explocion(fire.x,fire.y,basesize*25,Color.RED));
							startSoundPool(6);
							
							/*startSound(R.raw.sound5);*/
							switch(getRandom(0,15)){
								case 5:
									itemSpeed.add(new ItemSpeed(fire.x,fire.y));
									break;
								case 1:
									itemtriple.add(new ItemTriple(fire.x,fire.y));
									break;
								case 7:
									list_nuclear.add(new ItemNuclear(fire.x,fire.y));
									break;
								case 11:
									list_pluslife.add(new PlusLifeNave(fire.x,fire.y));
									break;
								case 12:
									list_pluslife_muro.add(new PlusLifeMuro(fire.x,fire.y));
							}
						}
						}
					}
				}
			}
		}
		
		for(PlusLifeNave pl :list_pluslife){
			pl.move(canvas);
			if(isClick(nave.x,nave.y,pl.x,pl.y,20)){
				pl.consumir();
				nave.life(1);
				textfades.add(new TextFade("LIFE "+nave.life,pl.x,pl.y,0xff00ff00));
			}
		}
		
		for(PlusLifeMuro plm :list_pluslife_muro){
			plm.move(canvas);
			if(isClick(nave.x,nave.y,plm.x,plm.y,20)){
				plm.consumir();
				++muro.life;
				textfades.add(new TextFade("LIFE "+muro.life,plm.x,plm.y,0xff0000ff));
			}
		}
		
		for(Block block:blocks){
			if(block.y>getHeight()-muro.height){
				
				if(--muro.life>=0){
					
					//startSound(R.raw.sound3);
					startSoundPool(3);
					if(muro.life==0){
						muro.paint.setColor(0xffff0000);
						muro.paint1.setColor(0xff200000);
						if(!nave.isEliminada()){
							naveDestroy();
						}
						
						isGameOver=true;
					}
				}
				block.eliminar();
				
				
			}
		}
		
		for(Asteroide a :asteroides){
			a.draw(canvas);
			
			for(Fire fire:fires){
				if(!a.isEliminada()){
				if(isClick(fire.x,fire.y,a.x,a.y,a.radio)){
					fire.eliminar();
					a.eliminar();
					startSoundPool(7);
					explocionList.add(new Explocion2(a.x,a.y));
				}
				}
			}
			if(!nave.isEliminada()){
			if(isClick(nave.x,nave.y,a.x,a.y,(nave.bitmap.getHeight()/2))){
				nave.life(-1);
				explocionList.add(new Explocion2(a.x,a.y));
				a.eliminar();
				startSoundPool(1);
				if(nave.life<=0){
				naveDestroy();
				toast("Tu nave fue destruida");
				textfades.add(new TextFade("Tu nave explotó!!!",(float)getWidth()/2,(float)getHeight()/2,0xffff6000));
				}
			}
			}
			if(a.y>getHeight()-muro.height){
				explocionList.add(new Explocion2(a.x,a.y));
				a.eliminar();
			}
		}
		
		for(Explocion2 exp :explocionList){
			exp.draw(canvas);
		}
		
		
		for(ItemNuclear nuclear :list_nuclear){
			nuclear.draw(canvas);
			if(nuclear.y>getHeight() -muro.height){
				nuclear.eliminar();
				muro.life=0;
				isGameOver=true;
				explocionList.add(new Explocion2(nuclear.x,nuclear.y));
				//startSound(R.raw.sound1);
				//startSound(R.raw.sound4);
				startSoundPool(1);
				startSoundPool(4);
				muro.paint.setColor(0xff101010);
			}
			
			for(Fire fire:fires){
				if((!fire.isEliminada())&&isClick(fire.x,fire.y,nuclear.x,nuclear.y,basesize*10)){
					nuclear.eliminar();
					fire.eliminar();
					//startSound(R.raw.sound4);
					startSoundPool(4);
					list_explocion.add(new Explocion(nuclear.x,nuclear.y,basesize*25,0xffffc000));
					for(float i = -1;i<2;i=i+0.2f){
						Fire tmpfire = new Fire(nuclear.x,nuclear.y);
						tmpfire.setColor(0xffffc000);
						tmpfire.dx=i;
						tmp_list_fire.add(tmpfire);
						
						tmpfire = new Fire(nuclear.x,nuclear.y);
						tmpfire.setColor(0xffffc000);
						tmpfire.dx=i;
						tmpfire.dy=-tmpfire.dy;
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
		list_nuclear
		);
		
		for(int i = 0;i<list_pluslife.size();i++){
			PlusLifeNave pl = list_pluslife.get(i);
			if(pl.consumido){
				list_pluslife.remove(i);
			}
		}
		
		for(int i = 0;i<list_pluslife_muro.size();i++){
			PlusLifeMuro pl = list_pluslife_muro.get(i);
			if(pl.consumido){
				list_pluslife_muro.remove(i);
			}
		}
		
		for(int i = 0;i<list_enemigo2.size();i++){
			if(list_enemigo2.get(i).anulable){
				list_enemigo2.remove(i);
			}
		}
		
		for(int i = 0;i<list_boss.size();i++){
			if(list_boss.get(i).anulable){
				list_boss.remove(i);
			}
		}
		
	}
	
	public void loadItem(){
		if(listitem.size()>0){
			String type = (String)listitem.get(0).getTag();
			int duration = listitem.get(0).getDuration();
			switch(type){
				case "triple":
					
					break;
			}
		}
	}
	
	void setTextFadeCenter(String text,int color){
		textfades.add(new TextFade(text,rectf_game.centerX(),rectf_game.centerY(),color));
	}
	
	
	
	boolean recordNow = true;
	public void naveDestroy(){
			
			nave.eliminar();
			explocionList.add(new Explocion2(nave.x,nave.y));
		
		if(recordNow){
			if(record<puntos){
				recordNow=false;
				gameData.edit().putInt("record",puntos).commit();
				toast("Nuevo record");
			}
		}
	}
	
	
	public void limpiar(Object...object){
		for(Object obj:object){
			ArrayList<UnidadBase>ub=(ArrayList<UnidadBase>)obj;
		for(int i = 0;i<ub.size();i++){
			if(ub.get(i).isEliminada){
				ub.remove(i);
			}
		}
		}
	}
	
	
	private class Fire extends UnidadBase{
		public float x,y;
		private Paint paint;
		int life = 1;
		float speed = 3;
		float dx = 0;
		float dy = -1;
		Fire(float x,float y){
			this.x=x;
			this.y=y;
			paint=new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(Color.RED);
		}
		/*
		public void start(Canvas canvas){
			y=y-3;
			canvas.drawCircle(x,y,5,paint);
		}
		*/
		void draw(Canvas canvas){
			float mx,my;
			if(dx==0){
				mx=0;
			}else{
				mx=dx*speed;
			}
			
			if(dy==0){
				my=0;
			}else{
				my=dy*speed;
			}
			x=x+mx;
			y=y+my;
			canvas.drawCircle(x,y,(basesize*5),paint);
			
		}
		
		
		void setColor(int color){
			paint.setColor(color);
		}
	}
	
	
	
	
	private class Block extends UnidadBase{
		int life = 5;
		public float x,y;
		private Paint paint;
		Paint paint2;
		float speed=1;
		float w,h;
		float round;
		
		float var_5,var_10;
		Block(float x,float y){
			this.x=x;
			this.y=y;
			w=basesize*25;
			h=basesize*25;
			round=basesize*5;
			paint=new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(Color.RED);
			
			paint2=new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setColor(0x90202020);
			
			var_5=basesize*5;
			var_10=basesize*10;
		}

		public void start(Canvas canvas){
			
			paint.setColor(getColorLevel(life>0?life-1:0));
			y=y+speed;
			RectF rectf = new RectF(x,y,x+w,y+h);
			RectF rectf1 = new RectF(x+var_5,y+var_5,x+var_10,y+var_10);
			RectF rectf2 = new RectF(x+w-var_10,y+var_5,x+w-var_5,y+var_10);
			RectF rectf3 = new RectF(x+var_5,y+h-var_10,x+w-var_5,y+h-var_5);
			
			canvas.drawRoundRect(rectf,round,round,paint);
			canvas.drawRect(rectf1,paint2);
			canvas.drawRect(rectf2,paint2);
			canvas.drawRect(rectf3,paint2);
		}
	}
	
	
	public static float getDip(Context _context, int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, _context.getResources().getDisplayMetrics());
	}
	
	public static int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	public class Nave extends UnidadBase{
		float x = 0,y=0;
		int speedAtak = 25;
		int life = 1;
		Paint paint, paint2, paint3, paint4, paint5;
		Bitmap bitmap;
		float animationTime = 0;
		float engineGlow = 0;
		
		Nave(){
			paint= new Paint(Paint.ANTI_ALIAS_FLAG);
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
			paint3.setShadowLayer(8*basesize, 0, 0, 0xffff0000);
			
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
			
			bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.nave_small);
		}
		
		public void draw(Canvas canvas,float x,float y){
			this.x=x;
			this.y=y;
			drawBitmapCenter(canvas,bitmap,basesize,x,y,paint);
		}
		
		public void draw2(Canvas canvas,float x,float y){
			this.x=x;
			this.y=y;
			animationTime += 0.03f;
			engineGlow = 0.5f + (float)Math.sin(animationTime * 4) * 0.5f;
			
			// Dibujar escudo energético
			int shieldAlpha = 100 + (int)((float)Math.sin(animationTime * 2) * 50);
			paint5.setAlpha(shieldAlpha);
			canvas.drawCircle(x, y, n.n30, paint5);
			paint5.setAlpha(255);
			
			// Dibujar cuerpo principal - nave espacial mejorada
			Path shipBody = new Path();
			
			// Fuselaje principal
			shipBody.moveTo(x, y - n.n20);
			shipBody.lineTo(x - n.n8, y - n.n10);
			shipBody.lineTo(x - n.n15, y);
			shipBody.lineTo(x - n.n8, y + n.n15);
			shipBody.lineTo(x - n.n4, y + n.n20);
			shipBody.lineTo(x, y + n.n15);
			shipBody.lineTo(x + n.n4, y + n.n20);
			shipBody.lineTo(x + n.n8, y + n.n15);
			shipBody.lineTo(x + n.n15, y);
			shipBody.lineTo(x + n.n8, y - n.n10);
			shipBody.close();
			
			// Aplicar color según nivel de vida
			paint.setColor(getColorLevel(life-1));
			canvas.drawPath(shipBody, paint);
			canvas.drawPath(shipBody, paint2);
			
			// Dibujar cabina
			canvas.drawCircle(x, y - n.n5, n.n4, paint4);
			canvas.drawCircle(x, y - n.n5, n.n2, paint);
			
			// Dibujar motores con efecto de pulsación
			float engineSize = n.n3 * (1 + engineGlow * 0.3f);
			
			// Motor izquierdo
			canvas.drawCircle(x - n.n8, y + n.n20, engineSize, paint3);
			canvas.drawCircle(x - n.n8, y + n.n20, n.n2, paint);
			
			// Motor derecho
			canvas.drawCircle(x + n.n8, y + n.n20, engineSize, paint3);
			canvas.drawCircle(x + n.n8, y + n.n20, n.n2, paint);
			
			// Motor central
			canvas.drawCircle(x, y + n.n20 - n.n2, engineSize * 0.8f, paint3);
			canvas.drawCircle(x, y + n.n20 - n.n2, n.n2, paint);
			
			// Dibujar llamas de los motores
			for(int i = 0; i < 3; i++){
				float flameOffset = (float)Math.sin(animationTime * 8 + i * 2) * n.n2;
				float flameX = x + (i - 1) * n.n8;
				float flameY = y + n.n25 + flameOffset;
				
				Path flame = new Path();
				flame.moveTo(flameX - n.n1, flameY);
				flame.lineTo(flameX, flameY + n.n8 + engineGlow * n.n4);
				flame.lineTo(flameX + n.n1, flameY);
				flame.close();
				
				paint3.setAlpha(150 + (int)(engineGlow * 100));
				canvas.drawPath(flame, paint3);
				paint3.setAlpha(255);
			}
			
			// Dibujar armas laterales
			canvas.drawRect(x - n.n20, y - n.n2, x - n.n15, y + n.n2, paint2);
			canvas.drawRect(x + n.n15, y - n.n2, x + n.n20, y + n.n2, paint2);
			
			// Dibujar puntos de energía giratorios
			for(int i = 0; i < 6; i++){
				float angle = -animationTime + (i * (float)Math.PI / 3);
				float px = x + (float)Math.cos(angle) * n.n12;
				float py = y + (float)Math.sin(angle) * n.n12;
				canvas.drawCircle(px, py, n.n1, paint4);
			}
			
			// Dibujar sensores frontales
			canvas.drawCircle(x - n.n5, y - n.n12, n.n1, paint2);
			canvas.drawCircle(x + n.n5, y - n.n12, n.n1, paint2);
		}
		
		public void life(int l){
			life=life+l;
			if(life>0){
				paint.setColor(getColorLevel(life-1));
			}
		}
		
		public Fire fire(){
			return new Fire(x,y);
		}
	}
	
	public static class UnidadBase{
		private boolean isEliminada=false;
		
		public boolean isEliminada(){
			return isEliminada;
		}
		
		public void eliminar(){
			isEliminada=true;
		}
	}
	
	public class ItemTriple extends Item {
		float x,y;
		Paint paint1, paint2, paint3;
		float giro = 0;
		float v5,v8,v10,v15,v20,v25;
		ItemTriple(float x,float y){
			super(5000);
			setTag("triple");
			this.x=x;
			this.y=y;
			
			// Paint principal - contorno brillante
			paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint1.setStyle(Paint.Style.STROKE);
			paint1.setStrokeWidth(basesize*3);
			paint1.setColor(0xff00ff00);
			paint1.setShadowLayer(10*basesize, 0, 0, 0xff00ff00);
			
			// Paint secundario - círculos internos
			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setStyle(Paint.Style.FILL);
			paint2.setColor(0xff80ff00);
			
			// Paint central - núcleo brillante
			paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint3.setStyle(Paint.Style.FILL);
			paint3.setColor(0xffffff00);
			paint3.setAlpha(180);
			
			v5=basesize*5;
			v8=basesize*8;
			v10=basesize*10;
			v15=basesize*15;
			v20=basesize*20;
			v25=basesize*25;
		}

		public void draw(Canvas canvas){
			y=y+basesize*2;
			giro += 0.05f;
			
			// Dibujar círculo central brillante
			canvas.drawCircle(x,y,v20,paint3);
			
			// Dibujar círculos orbitales con efecto de rotación
			for(int i=0; i<6; i++){
				float angle = giro + (i * (float)Math.PI / 3);
				float orbitX = x + (float)Math.cos(angle) * v15;
				float orbitY = y + (float)Math.sin(angle) * v15;
				canvas.drawCircle(orbitX, orbitY, v8, paint2);
			}
			
			// Dibujar contorno principal con efecto pulsante
			float pulse = v20 + (float)Math.sin(giro * 2) * 2;
			canvas.drawCircle(x,y,pulse,paint1);
			
			// Dibujar líneas conectoras
			for(int i=0; i<3; i++){
				float angle = giro + (i * (float)Math.PI * 2 / 3);
				float endX = x + (float)Math.cos(angle) * v25;
				float endY = y + (float)Math.sin(angle) * v25;
				canvas.drawLine(x, y, endX, endY, paint1);
			}
		}
	}
	
	public class ItemSpeed extends UnidadBase {
		float x,y;
		Paint paint1, paint2, paint3;
		float v2,v5,v8,v10,v15,v20;
		float rotation = 0;
		ItemSpeed(float x,float y){
			this.x=x;
			this.y=y;
			
			// Paint principal - flechas de velocidad
			paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint1.setStyle(Paint.Style.FILL);
			paint1.setColor(0xff00ffff);
			paint1.setShadowLayer(8*basesize, 0, 0, 0xff00ffff);
			
			// Paint secundario - contorno brillante
			paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint2.setStyle(Paint.Style.STROKE);
			paint2.setStrokeWidth(basesize*2);
			paint2.setColor(0xff0088ff);
			
			// Paint central - núcleo energético
			paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint3.setStyle(Paint.Style.FILL);
			paint3.setColor(0xffffffff);
			paint3.setAlpha(200);
			
			v2=basesize*2;
			v5=basesize*5;
			v8=basesize*8;
			v10=basesize*10;
			v15=basesize*15;
			v20=basesize*20;
		}
		
		public void draw(Canvas canvas){
			y=y+2;
			rotation += 0.1f;
			
			canvas.save();
			canvas.translate(x, y);
			canvas.rotate(rotation * 180 / (float)Math.PI);
			
			// Dibujar núcleo central brillante
			canvas.drawCircle(0, 0, v8, paint3);
			
			// Dibujar flechas de velocidad en 4 direcciones
			for(int i = 0; i < 4; i++){
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
			float pulse = v15 + (float)Math.sin(rotation * 3) * 3;
			canvas.drawCircle(0, 0, pulse, paint2);
			
			canvas.restore();
		}
	}
	
	public static boolean isClick(float X,float Y,float x,float y,float radio){
		float rx = X-x;
		float ry = Y-y;
		return java.lang.Math.hypot(rx,ry)<=radio;
	}
	
	public class TextFade extends UnidadBase{
		String texto;
		float x,y;
		float fade = 255;
		Paint paint;
		int color;
		public TextFade (String texto,float x,float y,int color){
			this.texto=texto;
			this.color=color;
			paint=new Paint(Paint.ANTI_ALIAS_FLAG);
			this.x=x;
			this.y=y;
			paint.setColor(color);
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setTypeface(paintText.getTypeface());
			paint.setTextSize(basesize*20);
		}
		
		public void draw(Canvas canvas){
			fade = fade-2.5f;
			paint.setAlpha((int)fade);
			y=y-0.25f;
			canvas.drawText(texto,x,y,paint);
			if(fade==0){
				eliminar();
			}
		}
	}
	
	public static int getColorLevel(int index){
		int[]colors={
			0xffff0000,
			0xffff8000,
			0xffffff00,
			0xff80ff00,
			0xff00ff00,
			0xff00c000
		};
		if(index>=colors.length){
			return 0xffffffff;
		}
		return colors[index];
	}
	
	public float getNivelSpeed(){
		float n[]={1,1.5f,3,4,5,5.5f,6.5f,7};
		return n[nivel];
	}
	
	/*
	public class Enemigo extends UnidadBase{
		float x=0,y=0;
		float x1=1,y1=0.30f;
		float speed = 2;
		
		void draw(Canvas canvas){
			float w = getWidth();
			float h = getHeight();
			float var_5=basesize*5;
			if(x>w||x<0){
				x1=-x1;
			}
			
			if(y>h||y<0){
				y1=-y1;
			}
			x=x+(x1*speed);
			y=y+(y1*speed);
			
			canvas.drawCircle(x,y,var_5,paintText);
		}
		
	}*/
	
	public class Muro extends UnidadBase{
		Paint paint;
		Paint paint1;
		int interval = 0;
		float height = 0;
		int life = 3;
		Muro(){
			paint=new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(0xff0000ff);
			paint1=new Paint(Paint.ANTI_ALIAS_FLAG);
			paint1.setColor(0xf0ffffff);
			height=basesize*32;
			
		}
		void draw(Canvas canvas){
			float w = getWidth();
			float h = getHeight();
			
			float var_16 = basesize*16;
			float var_32 = basesize*32;
			float var_8=basesize*8;
			
			RectF rectf = new RectF(0,h-var_32,w,h);
			canvas.drawRect(rectf,paint);
			canvas.drawLine(0,h-var_32,w,h-var_32,paint1);
			canvas.drawLine(0,h-var_16,w,h-var_16,paint1);
			canvas.drawLine(0,h,w,h,paint1);
			
			if(++interval==1000){
				interval=0;
			}
			if(interval==500){
				textfades.add(new TextFade("Bombas fuera",(float)getWidth()/2,(float)getHeight()/2,0xff0000ff));
			}
			for(float i = 0;i<getWidth()+var_16;i=i+var_16){
				canvas.drawLine(i,h-var_16,i,h,paint1);
				canvas.drawLine(i-var_8,h-var_32,i-var_8,h-var_16,paint1);
				
				if(interval==500){
					if(isEliminada()){return;}
					Fire fire = new Fire(i,getHeight());
					fire.paint.setColor(paint.getColor());
					fires.add(fire);
					
					
				}
			}
			
			
		}
	}
	
	public static Paint newPaint(){
		return new Paint(Paint.ANTI_ALIAS_FLAG);
	}
	
	public class Asteroide extends UnidadBase{
		float x,y;
		Bitmap bitmap;
		Paint paint;
		float speed=3;
		float radio;
		int w,h;
		Asteroide (float x,float y){
			this.x=x;
			this.y=y;
			bitmap=getBitmapRotate(BitmapFactory.decodeResource(getResources(),R.drawable.fire),180);
			paint=new Paint(Paint.ANTI_ALIAS_FLAG);
			
			w = bitmap.getWidth();
			h = bitmap.getHeight();
			radio = Math.max(w,h)/2;
			
		}
		
		void draw(Canvas canvas){
			y=y+speed;
			
			
			canvas.drawBitmap(bitmap,x-(w/2),y-(h/2),paint);
		}
	}
	
	public static Bitmap getBitmapRotate(final Bitmap src,float angle){
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		Bitmap bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
		
		return bitmap;
	}
	
	
	
	
	public  class Explocion2 extends UnidadBase{
		float x,y;
		ImageFrame img;
		int max = 14;
		int min = 0;
		public Explocion2(float x,float y){
			this.x=x-24.5f;
			this.y=y-24.5f;

			img=new ImageFrame(BitmapFactory.decodeResource(getResources(),R.drawable.explode_big),this.x,this.y);
			img.prepare(14,1);
		}

		void draw(Canvas canvas){
			img.draw(canvas);
			++min;
			if(min==max){
				eliminar();
			}
		}
	}

	
	
	public static class ImageFrame{
		Bitmap image;
		float x,y;
		int w,h;

		int filas = 1;
		int columnas=1;

		int indexFila = 1;
		int indexColumna=1;

		int numColumnas=1;
		int numFilas=1;

		public ImageFrame(Bitmap image,float x,float y){
			this.image=image;
			this.x=x;
			this.y=y;
		}

		public void cutImage(int x,int y,int w,int h){
			image=Bitmap.createBitmap(image,x,y,w,h);
		}

		public void prepare(int numC,int numF){
			columnas=numC;
			filas=numF;

			w=image.getWidth()/columnas;
			h=image.getHeight()/filas;

			numColumnas=image.getWidth()/w;
			numFilas=image.getHeight()/h;
		}

		public void draw(Canvas canvas){

			int indexX=(w*indexColumna)-w;
			int indexY=(h*indexFila)-h;

			Bitmap frame = Bitmap.createBitmap(image,indexX,indexY,w,h);

			canvas.drawBitmap(frame,x,y,new Paint(Paint.ANTI_ALIAS_FLAG));

			indexColumna=++indexColumna<=numColumnas?indexColumna:1;

		}
	}
	
	
	public static boolean isAfecte(float x,float y,float w,float h, float X,float Y){
		return X>x&&Y>y&&X<x+w&&Y<y+h;
	}
	
	
	public class Item extends UnidadBase{
		int duration;
		Object tag;
		public Item(int duration){
			this.duration=duration;
		}
		
		public void setDuration(int ms){
			duration=ms;
		}
		
		public int getDuration(){
			return duration;
		}
		
		public void setTag(Object tag){
			this.tag=tag;
		}
		
		public Object getTag(){
			return tag;
		}
	}
	
	void setMultifire(){
		++multiatak;
			getHandler().postDelayed(new Runnable(){
				@Override
				public void run()
				{
					--multiatak;
				}
			},5000);
	}
	
	public void toast(String str){
		Toast.makeText(getContext(),str,Toast.LENGTH_LONG).show();
	}
	
	public static class Explocion extends UnidadBase{
			float x,y,radio;
			int color;
			Paint paint;
			public Explocion(float x,float y,float radio,int color){
				this.x=x;
				this.y=y;
				this.radio=radio;
				this.color=color;
				paint=new Paint(Paint.ANTI_ALIAS_FLAG);
				paint.setColor(color);
			}
			float frames = 100;
			public void draw(Canvas canvas){
				paint.setAlpha((int)(255.0f*(frames/100)));
				canvas.drawCircle(x,y,radio-(radio*(frames/100)),paint);
				frames=frames-5;
				if(frames<=0){
					eliminar();
				}
			}
		}
		
		public class Base extends UnidadBase{
			float x,y;
			Base(float x,float y){
				this.x=x;
				this.y=y;
			}
		}
		
		public class ItemNuclear extends Base{
			Bitmap bitmap;
			Paint paint;
			public ItemNuclear(float x,float y){
				super(x,y);
				bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.nuclear);
				paint=new Paint(Paint.ANTI_ALIAS_FLAG);
				
			}
			
			public void draw(Canvas canvas){
				
				drawBitmapCenter(canvas,bitmap,basesize,x,++y,paint);
			}
		}
		
		public static void drawBitmapCenter(Canvas canvas,Bitmap bitmap,float basesize,float x,float y,Paint paint){
			float w=bitmap.getWidth()*basesize;
			float h=bitmap.getHeight()*basesize;
			float px = x-(w/2);
			float py = y-(h/2);
			canvas.drawBitmap(bitmap,px,py,paint);
		}
		
		private SoundPool soundpool;
		private Map <Integer,Integer> map_sound = new HashMap<>();
		private void createSoundPool(){
			if(Build.VERSION.SDK_INT > 21){
				SoundPool.Builder builder = new SoundPool.Builder();
				// El número de audio entrante
				builder.setMaxStreams(5);
				// AudioAttributes es un método para encapsular varios atributos de audio
				AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
				// Establecer los atributos apropiados de la secuencia de audio
				attrBuilder.setLegacyStreamType(AudioManager.STREAM_SYSTEM);//STREAM_MUSIC
				// Cargar un AudioAttributes
				builder.setAudioAttributes(attrBuilder.build());
				soundpool = builder.build();
			}else{
				soundpool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
			}
			
			map_sound.put(1,soundpool.load(getContext(),R.raw.sound1,1));
			map_sound.put(2,soundpool.load(getContext(),R.raw.sound2,1));
			map_sound.put(3,soundpool.load(getContext(),R.raw.sound3,1));
			map_sound.put(4,soundpool.load(getContext(),R.raw.sound4,1));
			map_sound.put(5,soundpool.load(getContext(),R.raw.sound5,1));
			map_sound.put(6,soundpool.load(getContext(),R.raw.sound6,1));
			map_sound.put(7,soundpool.load(getContext(),R.raw.sound7,1));
			
			
		}
		
		private void startSoundPool(int index){
			if(soundpool==null){
				createSoundPool();
			}
			
			soundpool.play(map_sound.get(index),1,1,0,0,1);
		}
		
		public static class BaseItem {
			public float x;
			public float y;
			Bitmap bitmap;
			Paint paint;
			public int w;
			public int h;
			boolean consumido = false;
			public BaseItem(Bitmap bitmap,float x,float y){
				this.bitmap=bitmap;
				this.x=x;
				this.y=y;
				paint=new Paint(Paint.ANTI_ALIAS_FLAG);
				w=bitmap.getWidth();
				h=bitmap.getHeight();
			}
			
			public void draw(Canvas canvas){
				canvas.drawBitmap(bitmap,x,y,paint);
			}
			
			public void move(Canvas canvas,float x,float y){
				this.x = this.x+x;
				this.y = this.y+y;
				draw(canvas);
			}
			
			public void consumir(){
				consumido=true;
			}
			
		    public void destroy(){
			consumido=true;
		}
			
		}
		
		
		public class PlusLifeNave extends BaseItem{
			
			PlusLifeNave(float x,float y){
				super(BitmapFactory.decodeResource(getResources(),R.drawable.plus_nave),x,y);
			}

			@Override
			public void consumir()
			{
				startSoundPool(2);
				super.consumir();
			}

			
			public void move(Canvas canvas)
			{
				if(consumido){
					return;
				}
				super.move(canvas,0,2);
				if(h>getHeight()){
					super.destroy();
				}
			}
			}
			
		public class PlusLifeMuro extends BaseItem{

			PlusLifeMuro(float x,float y){
				super(BitmapFactory.decodeResource(getResources(),R.drawable.plus_muro),x,y);
			}

			@Override
			public void consumir()
			{
				startSoundPool(2);
				super.consumir();
			}


			public void move(Canvas canvas)
			{
				if(consumido){
					return;
				}
				super.move(canvas,0,2);
				if(h>getHeight()){
					super.destroy();
				}
			}
			
		}
		
		public static class Texto{
			float x;
			float y;
			String texto;
			Paint paint1;
			Paint paint2;
			
			Texto(float x,float y){
				this.x=x;
				this.y=y;
				paint1=new Paint(Paint.ANTI_ALIAS_FLAG);
				paint2=new Paint(Paint.ANTI_ALIAS_FLAG);
				paint2.setStyle(Paint.Style.STROKE);
				paint1.setTextAlign(Paint.Align.CENTER);
				paint2.setTextAlign(Paint.Align.CENTER);
				setTextColor(0xff000000);
				setStrokeColor(0xffffffff);
				setStrokeWidth(1);
				
				
			}
			
			Texto(Texto texto){
				x=texto.x;
				y=texto.y;
				paint1=new Paint(texto.paint1);
				paint2=new Paint(texto.paint2);
				this.texto=texto.texto;
			}
			
			void setTypeface(Typeface typeface){
				paint1.setTypeface(typeface);
				paint2.setTypeface(typeface);
			}
			
			void setTextSize(float size){
				paint1.setTextSize(size);
				paint2.setTextSize(size);
			}
			
			void setText(String texto){
				this.texto = texto;
			}
			
			void setTextColor(int color){
				paint1.setColor(color);
			}
			void setStrokeWidth(float width){
				paint2.setStrokeWidth(width);
			}
			void setStrokeColor(int color){
				paint2.setColor(color);
			}
			void setStroke(int width,int color){
				setStrokeColor(color);
				setStrokeWidth(width);
			}
			void draw (Canvas canvas){
				canvas.drawText(texto,x,y,paint1);
				canvas.drawText(texto,x,y,paint2);
			}
			
			void darw(Canvas canvas,String texto){
				canvas.drawText(texto,x,y,paint1);
				canvas.drawText(texto,x,y,paint2);
			}
			
		    void darw(Canvas canvas,String texto,float x,float y){
				canvas.drawText(texto,x,y,paint1);
				canvas.drawText(texto,x,y,paint2);
		}
			
		}
		
		
	public class Enemigo2 extends Base2{
		RectF rectf;
		Paint paint, paint2, paint3;
		float mx, my;
		float animationTime = 0;
		float rotationSpeed = 0.02f;
		
		Enemigo2(float x,float y){
			super(x,y);
			rectf=new RectF(x,y,x+n.n25,y+n.n25);
			
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
			paint3.setShadowLayer(3*basesize, 0, 0, 0xffff0000);
		}
		
		void move (Canvas canvas){
			move(canvas,mx,my);
		}
		
		public void move(Canvas canvas, float x,float y){
			this.x=this.x+x;
			this.y=this.y+y;
			draw(canvas);
		}
		
		public void draw(Canvas canvas){
			animationTime += rotationSpeed;
			
			// Actualizar rectángulo del cuerpo
			rectf=new RectF(x,y,x+n.n25,y+n.n25);
			
			// Dibujar cuerpo principal - diamante rotante
			canvas.save();
			canvas.translate(x + n.n12, y + n.n12);
			canvas.rotate(animationTime * 180 / (float)Math.PI);
			
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
			float pulse = n.n3 + (float)Math.sin(animationTime * 3) * n.n1;
			canvas.drawCircle(x + n.n12, y + n.n12, pulse, paint3);
			
			// Dibujar ojos satélite rotantes
			for(int i = 0; i < 4; i++){
				float angle = -animationTime + (i * (float)Math.PI / 2);
				float orbitRadius = n.n8;
				float px = x + n.n12 + (float)Math.cos(angle) * orbitRadius;
				float py = y + n.n12 + (float)Math.sin(angle) * orbitRadius;
				
				// Ojo con efecto de parpadeo
				float eyeSize = (float)Math.sin(animationTime * 8 + i) > 0.7f ? n.n1 : n.n2;
				canvas.drawCircle(px, py, eyeSize, paint3);
				canvas.drawCircle(px, py, n.n1, paint);
			}
			
			// Dibujar anillo energético exterior
			int ringAlpha = 150 + (int)((float)Math.sin(animationTime * 2) * 50);
			paint2.setAlpha(ringAlpha);
			canvas.drawCircle(x + n.n12, y + n.n12, n.n15, paint2);
			paint2.setAlpha(255);
			
			// Dibujar pequeños deflectores
			for(int i = 0; i < 6; i++){
				float angle = animationTime * 2 + (i * (float)Math.PI / 3);
				float px = x + n.n12 + (float)Math.cos(angle) * n.n10;
				float py = y + n.n12 + (float)Math.sin(angle) * n.n10;
				canvas.drawCircle(px, py, n.n1, paint2);
			}
		}
	}

	public class Boss extends Base2{
		RectF rectf;
		Paint paint, paint2, paint3, paint4;
		float mx, my;
		float animationTime = 0;
		float shieldRadius = 0;
		
		Boss(float x,float y){
			super(x,y);
			rectf=new RectF(x,y,x+n.n40,y+n.n40);
			
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
			paint3.setShadowLayer(5*basesize, 0, 0, 0xffff0000);
			
			// Paint para escudo energético
			paint4 = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint4.setColor(0xff00ffff); // Cyan brillante
			paint4.setStyle(Paint.Style.STROKE);
			paint4.setStrokeWidth(n.get(2));
			paint4.setAlpha(150);
		}
		
		void move (Canvas canvas){
			move(canvas,mx,my);
		}
		
		public void move(Canvas canvas, float x,float y){
			this.x=this.x+x;
			this.y=this.y+y;
			draw(canvas);
		}
		
		public void draw(Canvas canvas){
			animationTime += 0.05f;
			shieldRadius = n.n30 + (float)Math.sin(animationTime) * n.n5;
			
			// Actualizar rectángulo del cuerpo
			rectf=new RectF(x-n.n20,y-n.n20,x+n.n20,y+n.n20);
			
			// Dibujar escudo energético animado
			paint4.setAlpha(100 + (int)((float)Math.sin(animationTime * 2) * 50));
			canvas.drawCircle(x, y, shieldRadius, paint4);
			
			// Dibujar cuerpo principal - hexágono
			Path hexagon = new Path();
			for(int i = 0; i < 6; i++){
				float angle = (float)Math.toRadians(60 * i);
				float px = x + (float)Math.cos(angle) * n.n20;
				float py = y + (float)Math.sin(angle) * n.n20;
				if(i == 0){
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
			for(int i = 0; i < 8; i++){
				float angle = animationTime + (i * (float)Math.PI / 4);
				float px = x + (float)Math.cos(angle) * n.n15;
				float py = y + (float)Math.sin(angle) * n.n15;
				canvas.drawCircle(px, py, n.n3, paint3);
			}
			
			// Dibujar cañones laterales
			float cannonOffset = (float)Math.sin(animationTime * 3) * n.n3;
			// Cañón izquierdo
			canvas.drawRect(x-n.n25-cannonOffset, y-n.n3, x-n.n15-cannonOffset, y+n.n3, paint);
			canvas.drawRect(x-n.n25-cannonOffset, y-n.n2, x-n.n15-cannonOffset, y+n.n2, paint3);
			
			// Cañón derecho
			canvas.drawRect(x+n.n15+cannonOffset, y-n.n3, x+n.n25+cannonOffset, y+n.n3, paint);
			canvas.drawRect(x+n.n15+cannonOffset, y-n.n2, x+n.n25+cannonOffset, y+n.n2, paint3);
			
			// Dibujar ojos brillantes
			float eyeBlink = (float)Math.sin(animationTime * 8) > 0.8f ? n.n2 : n.n4;
			canvas.drawCircle(x-n.n8, y-n.n5, eyeBlink, paint3);
			canvas.drawCircle(x+n.n8, y-n.n5, eyeBlink, paint3);
			canvas.drawCircle(x-n.n8, y-n.n5, n.n2, paint);
			canvas.drawCircle(x+n.n8, y-n.n5, n.n2, paint);
		}
	}

	public static class Base2{
		float life=1;
		float x;
		float y;
		boolean anulable = false;

		public Base2(){}
		public Base2(float x,float y){
			this.x=x;
			this.y=y;
		}

		public void anular(){
			anulable=true;
		}
	}
	
	public class N{
		final float n1,n2,n3,n4,n5,n8,n10,n12,n15,n20,n25,n30,n40;
		float base;
		N(float basesize){
			base=basesize;
			n1=get(1);
			n2=get(2);
			n3=get(3);
			n4=get(4);
			n5=get(5);
			n8=get(8);
			n10=get(10);
			n12=get(12);
			n15=get(15);
			n20=get(20);
			n25=get(25);
			n30=get(30);
			n40=get(40);
		}
		
		float get(float num){
			if(base == 0) return 0;
			return num*base;
		}
	}
}
