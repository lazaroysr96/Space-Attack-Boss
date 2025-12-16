package cu.spaceattack.boss;

import android.content.*;
import android.media.*;
import android.os.*;
import android.widget.*;

public class MusicEngine {
	Context ctx;
	MediaPlayer mp1;
	MediaPlayer mp2;
	MediaPlayer runSound;
	boolean isTarget = false;

	public MusicEngine(Context ctx) {
		this.ctx = ctx;
	}

	public void startBGMusic(int id) {
		if (mp1 == null && mp2 == null) {
			isTarget = true;
			mp1 = MediaPlayer.create(ctx, id);
			mp1.setLooping(true);
			mp1.start();
		} else if (isTarget) {
			if (isFading) {
				return;
			}
			isTarget = false;

			if (mp2 != null && mp2.isPlaying()) {
				mp2.stop();
			}
			mp2 = MediaPlayer.create(ctx, id);
			mp2.setLooping(true);
			mp2.setVolume(0.1f, 0.1f);
			mp2.start();

			fadeOutMusic(mp1);
			fadeInMusic(mp2);
		} else if (!isTarget) {
			if (isFading) {
				return;
			}
			isTarget = true;

			if (mp1 != null && mp1.isPlaying()) {
				mp1.stop();
			}
			mp1 = MediaPlayer.create(ctx, id);
			mp1.setLooping(true);
			mp1.setVolume(0.1f, 0.1f);
			mp1.start();

			fadeOutMusic(mp2);
			fadeInMusic(mp1);
		}
	}

	private Handler fadeHandler = new Handler();
	private boolean isFading = false;

	private void fadeOutMusic(final MediaPlayer mp) {
		if (isFading || mp == null)
			return;
		isFading = true;

		final int fadeDuration = 2000; // 2 segundos para fade out
		final int fadeSteps = 20;
		final float stepVolume = 0.5f / fadeSteps; // Volume actual (0.5) dividido en pasos
		final int stepDelay = fadeDuration / fadeSteps;

		Runnable fadeRunnable = new Runnable() {
			private int currentStep = 0;

			@Override
			public void run() {
				if (currentStep < fadeSteps && mp != null && mp.isPlaying()) {
					float newVolume = 0.5f - (stepVolume * currentStep);
					if (newVolume < 0)
						newVolume = 0;
					mp.setVolume(newVolume, newVolume);

					currentStep++;
					fadeHandler.postDelayed(this, stepDelay);
				} else {
					// Fade out completado
					if (mp != null) {
						mp.setVolume(0f, 0f);
					}
					isFading = false;

					if (mp1.equals(mp)) {
						mp1.stop();
						mp1.release();
						mp1 = null;
					} else if (mp2.equals(mp)) {
						mp2.stop();
						mp2.release();
						mp2 = null;
					}
					/*
					 * if (onComplete != null) {
					 * onComplete.run();
					 * }
					 */
				}
			}
		};

		fadeHandler.post(fadeRunnable);
	}

	private void fadeInMusic(final MediaPlayer mp) {
		if (mp == null)
			return;

		if (runSound != null && runSound.isPlaying()) {
			return;
		}

		final int fadeDuration = 1500; // 1.5 segundos para fade in
		final int fadeSteps = 15;
		final float stepVolume = 1.0f / fadeSteps;
		final int stepDelay = fadeDuration / fadeSteps;

		Runnable fadeRunnable = new Runnable() {
			private int currentStep = 0;

			@Override
			public void run() {
				if (currentStep < fadeSteps && mp != null && mp.isPlaying()) {
					float newVolume = stepVolume * currentStep;
					if (newVolume > 1.0f)
						newVolume = 1.0f;
					mp.setVolume(newVolume, newVolume);

					currentStep++;
					fadeHandler.postDelayed(this, stepDelay);
				} else {
					// Fade in completado
					if (mp != null) {
						mp.setVolume(1.0f, 1.0f);
					}
				}
			}
		};

		fadeHandler.post(fadeRunnable);
	}

	public void pause() {
		if (mp1 != null) {
			mp1.pause();
		}

		if (mp2 != null) {
			mp2.pause();
		}

		if (runSound != null) {
			runSound.pause();
		}
	}

	public void restart() {
		if (mp1 != null) {
			mp1.start();
		}

		if (mp2 != null) {
			mp2.start();
		}

		if (runSound != null) {
			runSound.start();
		}
	}

	public void runSound(int id) {
		getMusic().setVolume(0.4f, 0.4f);
		runSound = MediaPlayer.create(ctx, id);
		runSound.setVolume(1, 1);
		runSound.start();
		runSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer p1) {
				if (runSound != null) {
					runSound.stop();
					runSound.release();
					runSound = null;
				}
				fadeInMusic(getMusic());
			}
		});
	}

	public MediaPlayer getMusic() {
		return isTarget ? mp1 : mp2;
	}

}
