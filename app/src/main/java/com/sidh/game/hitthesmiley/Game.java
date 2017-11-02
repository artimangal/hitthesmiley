package com.sidh.game.hitthesmiley;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

public class Game {

	// Screen info
	public static int screenWidth;
	public static int screenHeight;
	public static float screenDensity;

	private boolean gameOver;

	// We will need this to draw background image full screen.
	private Rect destBackgroundImage;
	private Paint paintForImages;
	// We need this for the grass, so that it will be resized to the width of a
	// screen when drawn.
	private Rect destGressImage;

	// Images
	private static Bitmap backgroundImage;
	private static Bitmap grassImage;
	public static Bitmap duckImage;
	public static Bitmap duckRightImage;

	// List of all ducks on a screen.
	private ArrayList<Duck> aliveDucks;

	// How many ducks were killed?
	private int duckKilled;

	// Color and size for text.
	private Paint paintText;
	private int textSize;

	// Needed for new random coordinates.
	private Random random = new Random();

	// Position of text for restarting the game.
	private float textForRestart_x;
	private float textForRestart_y;
	Context context;

	public Game(int screenWidth, int screenHeight, Resources resources,
			Context context) {
		Game.screenWidth = screenWidth;
		Game.screenHeight = screenHeight;
		Game.screenDensity = resources.getDisplayMetrics().density;
		this.context = context;

		this.LoadContent(resources);

		destBackgroundImage = new Rect(0, 0, screenWidth, screenHeight);
		destGressImage = new Rect(0,
				screenHeight - Game.grassImage.getHeight(), screenWidth,
				screenHeight);

		paintForImages = new Paint();
		paintForImages.setFilterBitmap(true);

		aliveDucks = new ArrayList<Duck>();

		textSize = 25;
		paintText = new Paint();
		paintText.setColor(Color.BLACK);
		paintText.setTextSize(textSize * Game.screenDensity);

		textForRestart_x = Game.screenWidth / 2 - 95 * Game.screenDensity;
		textForRestart_y = Game.screenHeight / 2 + 80;

		this.ResetGame();
	}

	/**
	 * Load files.
	 */
	private void LoadContent(Resources resources) {
		backgroundImage = BitmapFactory
				.decodeResource(resources, R.drawable.bg);
		grassImage = BitmapFactory.decodeResource(resources, R.drawable.grass);
		duckImage = BitmapFactory.decodeResource(resources, R.drawable.smiley);

		// Image for ducks that come from left side of a screen.
		Matrix matrix = new Matrix();
		matrix.preScale(-1.0f, 1.0f);
		duckRightImage = Bitmap.createBitmap(duckImage, 0, 0,
				duckImage.getWidth(), duckImage.getHeight(), matrix, false);
	}

	/**
	 * For (re)setting some game variables before game can start.
	 */
	public void ResetGame() {
		gameOver = false;

		aliveDucks.clear();

		Duck.speed = Duck.initSpeed;
		Duck.timeBetweenDucks = Duck.initTimeBetweenDucks;
		Duck.timeOfLastDuck = 0;

		Duck.timeOfLastSpeedup = 0;

		duckKilled = 0;

		// We create some starting ducks.
		this.addNewDuck();
		this.addNewDuck();
	}

	/**
	 * Game update method.
	 * 
	 * @param gameTime
	 *            Elapsed game time in milliseconds.
	 */
	public void Update(long gameTime) {
		if (gameOver) {
			return;
		}

		// Create new duck, if time.
		if ((gameTime - Duck.timeOfLastDuck) > Duck.timeBetweenDucks) {
			Duck.timeOfLastDuck = gameTime;

			this.addNewDuck();
		}

		// Update ducks
		for (int i = 0; i < aliveDucks.size(); i++) {
			Duck duck = aliveDucks.get(i);

			duck.update();

			// Check if any duck got away and if did end game.
			if (duck.x > Game.screenWidth
					|| duck.x < 0 - Game.duckImage.getWidth()) {
				gameOver = true;

				if (duckKilled > HighScore.highScore) {
					// New high score
					HighScore.highScore = duckKilled;
					// Save new high score to file.
					HighScore.saveHighScore();
				}
			}
		}

		// Speedup the game, if time
		if ((gameTime - Duck.timeOfLastSpeedup) > Duck.timeBetweenSpeedups) {
			Duck.timeOfLastSpeedup = gameTime;

			Duck.speed += 0.03;
			if (Duck.timeBetweenDucks > (0.5 * 1000))
				Duck.timeBetweenDucks -= 90;
		}
	}

	/**
	 * Draw the game to the screen.
	 * 
	 * @param canvas
	 *            Canvas on which we will draw.
	 */
	public void Draw(Canvas canvas) {
		// First we need to erase everything we draw before.
		canvas.drawColor(Color.BLACK);

		// Draw background image.
		canvas.drawBitmap(Game.backgroundImage, null, this.destBackgroundImage,
				this.paintForImages);

		// Draw ducks
		for (int i = 0; i < aliveDucks.size(); i++) {
			aliveDucks.get(i).draw(canvas);
		}

		// Draw grass
		canvas.drawBitmap(Game.grassImage, null, this.destGressImage,
				this.paintForImages);

		// Draw how many ducks was killed.
		paintText.setColor(Color.parseColor("#375133"));
		paintText.setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
		canvas.drawText("Score: " + Integer.toString(this.duckKilled),
				120.0f * Game.screenDensity, 70.0f * Game.screenDensity,
				paintText);
		canvas.drawText("High score: " + Integer.toString(HighScore.highScore),
				120.0f * Game.screenDensity, textSize * 4 * Game.screenDensity,
				paintText);

		if (gameOver) {
			canvas.drawText("Game Over !!", Game.screenWidth / 2 - 65
					* Game.screenDensity, Game.screenHeight / 2 - 30, paintText);
			canvas.drawText("Touch to restart", textForRestart_x,
					textForRestart_y, paintText);
		}
	}

	/**
	 * When touch on screen is detected.
	 * 
	 * @param event
	 *            MotionEvent
	 */
	public void touchEvent_actionDown(MotionEvent event) {
		if (!gameOver) {
			this.checkIfAnyDuckShooted(event.getX(), event.getY());
		} else {
			// You can check if this coordinates are correct by drawing
			// rectangle with this coordinates.
			// They are not exactly over the text but it's close enough :)
			if (event.getX() > textForRestart_x
					&& event.getX() < textForRestart_x + 480
					&& event.getY() > textForRestart_y - 50
					&& event.getY() < textForRestart_y + 50) {
				if (context instanceof MainMenu) {
					Intent intent = new Intent(context, FANActivity.class);
					intent.putExtra("caller_type", "game_over");
					((MainMenu) context).startActivityForResult(intent, 7001);
				}
				// this.ResetGame();
			}
		}
	}

	/**
	 * When moving on screen is detected.
	 * 
	 * @param event
	 *            MotionEvent
	 */
	public void touchEvent_actionMove(MotionEvent event) {

	}

	/**
	 * When touch on screen is released.
	 * 
	 * @param event
	 *            MotionEvent
	 */
	public void touchEvent_actionUp(MotionEvent event) {

	}

	/**
	 * Checks if there is any duck on given coordinates. If any duck was shoot
	 * it is removed from the array list.
	 * 
	 * @param touchX
	 *            X coordinate of the touch.
	 * @param touchY
	 *            Y coordinate of the touch.
	 */
	private void checkIfAnyDuckShooted(float touchX, float touchY) {
		for (int i = 0; i < aliveDucks.size(); i++) {
			Duck duck = aliveDucks.get(i);

			if (duck.wasItShoot((int) touchX, (int) touchY)) {
				aliveDucks.remove(i);
				duckKilled++;
			}
		}
	}

	/**
	 * Creates new y coordinate for new duck.
	 * 
	 * @return int value somewhere on water.
	 */
	private int newYcoordinate() {
		// This edge values are not very accrued but are close enough :)
		int min = (Game.screenHeight / 2) + Game.duckImage.getHeight();
		int max = Game.screenHeight - Game.grassImage.getHeight();

		int height = max - min;

		int newYcoordiante = this.random.nextInt(height) + min;

		return newYcoordiante;
	}

	/**
	 * Creates a new duck and add it on array list.
	 */
	private void addNewDuck() {
		this.aliveDucks.add(new Duck(newYcoordinate()));
	}

}
