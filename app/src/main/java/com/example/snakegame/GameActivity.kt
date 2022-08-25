package com.example.snakegame


import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.example.snakegame.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var gameView: GameView

    private var score: Int = 0
    private var isGameOver: Boolean = false
    private var bgm: MediaPlayer? = null

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        bgm = MediaPlayer.create(this, R.raw.bicycle)

        gameView = binding.gameView
        gameView.init()

        binding.gameScoreTv.post {
            binding.gameScoreTv.text = getString(R.string.game_score, score.toString())
        }

        addListeners()
        startGame()
    }

    private fun addListeners() {
        binding.gameUpBtn.setOnClickListener {
            gameView.setDirection(Direction.UP)
        }

        binding.gameDownBtn.setOnClickListener {
            gameView.setDirection(Direction.DOWN)
        }

        binding.gameLeftBtn.setOnClickListener {
            gameView.setDirection(Direction.LEFT)
        }

        binding.gameRightBtn.setOnClickListener {
            gameView.setDirection(Direction.RIGHT)
        }
    }

    private fun startGame() {
        Thread {
            while (!isGameOver) {
                try {
                    Thread.sleep(400)
                    startBgm()
                    next()
                    handler.post(gameView::invalidate)
                }
                catch (ignored: InterruptedException) { }
            }

            val intent = Intent(this, GameOverActivity::class.java)
            intent.putExtra("score", score)
            startActivity(intent)
            finish()
        }.start()
    }

    private fun startBgm() {
        Thread {
            while(!isGameOver) {
                try {
                    Thread.sleep(100)
                    bgm?.start()
                }
                catch (ignored: InterruptedException) { }
            }
        }.start()
    }

    private fun next() {
        val snake = gameView.getSnake()
        val firstCell = snake.first
        val nextCell = gameView.getNext(firstCell)

        when (nextCell.type) {
            CellType.EMPTY -> {
                nextCell.type = CellType.SNAKE
                snake.addFirst(nextCell)
                snake.last.type = CellType.EMPTY
                snake.removeLast()
            }

            CellType.APPLE -> {
                nextCell.type = CellType.SNAKE
                snake.addFirst(nextCell)
                gameView.randomApple()

                score += 10
                binding.gameScoreTv.post {
                    binding.gameScoreTv.text = getString(R.string.game_score, score.toString())
                }
            }

            CellType.SNAKE -> {
                isGameOver = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bgm?.release()
        bgm = null
    }
}