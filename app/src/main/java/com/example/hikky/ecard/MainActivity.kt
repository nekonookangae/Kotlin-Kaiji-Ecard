package com.example.hikky.ecard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val tag = ""
    private var playerWins = 0             // プレイヤーの勝ち数
    private var cpuWins = 0                // コンピュータの勝ち数
    private var gameStart = false          // ゲームを開始したかどうか
    private var gameEnd = true             // ゲームを終了したか(又は始まっていないか)どうか
    private var turnend = false            // ターンが終了したかどうか
    private var isThinking = false         // プレイヤーが考え中かどうか
    private var round = 0                  // ラウンド数
    private var turn = 0                   // ターン数
    // 皇帝側の手札をEmperors, 奴隷側の手札をSlavesとする
    private val emperors = mutableListOf('E', 'C', 'C', 'C', 'C')
    private val slaves = mutableListOf('S', 'C', 'C', 'C', 'C')
    // 手札は初期化されるまで分からないので、lateinitを使う
    private lateinit var playerHands: MutableList<Efield>
    private lateinit var cpuHands: MutableList<Efield>
    // 場に出すカードも最初は分からないが、「プレイヤーが時間内に出さない」をnullで判定するため
    // lateinitは使っていない
    private var playerField: Efield? = null
    private var cpuField: Efield? = null
    // timerもターンが開始するまでは初期化できない
    private var timer: CountDownTimer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resultText.visibility = View.INVISIBLE
        resultText2.visibility = View.INVISIBLE

        /* 画像を180度回転する処理(重いので未使用)
        Image View imageView = findViewById(R.id.[ImageViewのID]);
        Bitmap bitmap_origin = BitmapFactory.decodeResource(getResources(), [画像パス]);

        int imageWidth = bitmap_origin.getWidth();
        int imageHeight = bitmap_origin.getHeight();

        Matrix matrix = new Matrix();

        matrix.setRotate(180, imageWidth/2, imageHeight/2);

        Bitmap bitmap_rotate = Bitmap.createBitmap(bitmap_origin, 0, 0, imageWidth, imageHeight, matrix, true);

        imageView.setImageBitmap(bitmap_rotate);
        */

        // 次のターンもしくはラウンドに移る
        startBtn.setOnClickListener {
            if (gameEnd && !gameStart) {
                gameStart = true
                gameEnd = false
                resultText.visibility = View.INVISIBLE
                resultText2.visibility = View.INVISIBLE
                Log.d(tag, "Game start ")
                play()
                playerWins = 0
                cpuWins = 0
                round = 0
                turn = 0
                cpuWinsText.text = "COM: 0勝"
                playerWinsText.text = "Player: 0勝"
            }
        }

        // 次のターンもしくはラウンドに移る
        nextBtn.setOnClickListener {
            if (gameEnd) {
                val result = when (playerWins - cpuWins) {
                    in 1..Int.MAX_VALUE -> "YOU WIN!"
                    0 -> "DRAW"
                    else -> "YOU LOSE.."
                }
                resultText2.text = result
                resultText.visibility = View.VISIBLE
                resultText2.visibility = View.VISIBLE
            }
            else {
                if (turnend) {
                    play()
                }
            }
        }

        // プレイヤーの考え中かつそのカードがまだ場に出されていなければ選べる
        playerCard1.setOnClickListener {
            if (isThinking && !playerHands[0].isRemove) {
                playerField = selectHand(playerHands[0], playerBoard)
                timer?.onFinish()
            }
        }

        playerCard2.setOnClickListener {
            if (isThinking && !playerHands[1].isRemove) {
                playerField = selectHand(playerHands[1], playerBoard)
                timer?.onFinish()
            }
        }
        playerCard3.setOnClickListener {
            if (isThinking && !playerHands[2].isRemove) {
                playerField = selectHand(playerHands[2], playerBoard)
                timer?.onFinish()
            }
        }
        playerCard4.setOnClickListener {
            if (isThinking && !playerHands[3].isRemove) {
                playerField = selectHand(playerHands[3], playerBoard)
                timer?.onFinish()
            }
        }
        playerCard5.setOnClickListener {
            if (isThinking && !playerHands[4].isRemove) {
                playerField = selectHand(playerHands[4], playerBoard)
                timer?.onFinish()
            }
        }
    }


    // ゲーム開始時、変数の値を初期化する
    /* override fun onResume() {
        super.onResume()
    }*/

    // ゲーム開始時とラウンド入れ替え時に呼び出される
    // 簡単のため、プレイヤーが皇帝側からスタートする
    // 3回ずつ皇帝と奴隷を入れ替えて戦う(1~3: 皇帝, 4~6: 奴隷..)
    private fun drawCard() {
        // プレイヤーとコンピュータの手札をシャッフルして渡す
        val playerDraws = if (round%6 < 3) emperors.shuffled() else slaves.shuffled()
        val cpuDraws = if (round%6 < 3) slaves.shuffled() else emperors.shuffled()

        // playerとcpuの手札をFieldのリストで管理、フィールドを描画
        playerHands = assignCards(playerDraws, true)
        cpuHands = assignCards(cpuDraws, false)

        Log.d(tag, "Round: ${round+1}")
    }

    // 手札cardsとプレイヤーかどうかを表すisPlayerによって、互いのフィールドを生成する関数
    private fun assignCards(cards: List<Char>, isPlayer: Boolean): MutableList<Efield> {
        val fields = mutableListOf<Efield>()
        val playerBtns = listOf(playerCard1, playerCard2, playerCard3, playerCard4, playerCard5)
        val cpuBtns = listOf(cpuCard1, cpuCard2, cpuCard3, cpuCard4, cpuCard5)

        for (i in cards.indices) {
            playerBtns[i].visibility = View.VISIBLE
            cpuBtns[i].visibility = View.VISIBLE
            val imbBtn = if (isPlayer) playerBtns[i] else cpuBtns[i]
            // cpuは裏のまま描画したいので、isPlayerの逆のBool値をとっている
            fields.add(Efield(cards[i], imbBtn, !isPlayer))
        }
        return fields
    }

    // カードを出し合う
    private fun play(){
        turnend = false
        // ターン開始時、互いにカードは選択しておらず、フィールドにはカードがないように見せる
        playerField = null
        cpuField = null
        playerBoard.setImageResource(R.drawable.reverse)
        cpuBoard.setImageResource(R.drawable.reverse_inv)
        playerBoard.visibility = View.INVISIBLE
        cpuBoard.visibility = View.INVISIBLE

        // ラウンド開始時には手札を入れ替える
        if (turn == 0) {
            drawCard()
        }
        // 各ターンにおいて、1,3回目は皇帝側が、2,4回目は奴隷側が先に引く
        // ラウンド1~3と7~9はプレイヤーが皇帝側、4~6と10~12はプレイヤーが奴隷側
        if ((round % 6 < 3 && turn % 2 == 0) || (round % 6 >= 3 && turn % 2 != 0)) {
            thinking(true)
            Log.d(tag, "先攻です")
        } else {
            thinking(false)
            Log.d(tag, "後攻です")
        }
    }

    // カードを手札から取り除き、フィールドに置く関数
    private fun thinking(isFirst: Boolean) {
        val rdm = Random.nextInt(cpuHands.size)
        if (isFirst) {
            timer = object : CountDownTimer(15000, 1000) {
                // 1秒刻みで残り時間を設定
                override fun onTick(millisUntilFinished: Long) {
                    val text = "00:%02d".format((millisUntilFinished+1000) / 1000 % 60)
                    limittimer.text = text
                }
                // 先攻の場合、選ぶかタイマーが終了した後にCPUが引く
                override fun onFinish() {
                    isThinking = false
                    cpuField = selectHand(cpuHands[rdm], cpuBoard)
                    cpuHands.removeAt(rdm)
                    judge()
                }
            }
        } else {
            // 後攻の場合、タイマーを生成する前にCPUが引く
            cpuField = selectHand(cpuHands[rdm], cpuBoard)
            cpuHands.removeAt(rdm)
            timer = object : CountDownTimer(15000, 1000) {
                // 1秒刻みで残り時間を設定
                override fun onTick(millisUntilFinished: Long) {
                    val text = "00:%02d".format((millisUntilFinished+1000) / 1000 % 60)
                    limittimer.text = text
                }
                override fun onFinish() {
                    isThinking = false
                    judge()
                }
            }
        }
        isThinking = true
        limittimer.text = "15:00"
        timer?.start()
    }

    private fun judge() {
        // timerを再利用するため、cancelとnull処理を行う
        timer?.cancel()
        timer = null
        limittimer.text = "00:00"
        // プレイヤーが制限時間内に選べなかった場合、強制的に1枚選ぶ
        if (playerField == null) {
            Log.d(tag, "時間切れです。強制的に選択します")
            for (i in playerHands.indices) {
                if (!playerHands[i].isRemove) {
                    playerField = selectHand(playerHands[i], playerBoard)
                    break
                }
            }
        }
        // 互いの出したカードを開示
        openField()
        Log.d(tag, "player: " + playerField?.rank.toString())
        Log.d(tag, "com: "+ cpuField?.rank.toString())
        // 勝ち負けの判定を行う
        when (playerField?.compareRank(cpuField?.rank)) {
            1 -> {
                playerWins++
                round++
                turn=0
                val text = "Player: $playerWins 勝"
                playerWinsText.text = text
            }
            -1 -> {
                cpuWins++
                round++
                turn=0
                val text = "COM: $cpuWins 勝"
                cpuWinsText.text = text
            }
            0 -> {
                turn++
            }
        }
        if (round > 0 && round % 6 == 0){
            nextBtn.text = "Result"
            gameEnd = true
            gameStart = false
        }
        turnend = true
    }

    // 手札が選択された時に呼び出される関数
    private fun selectHand(hand: Efield, board: ImageView): Efield {
        hand.isRemove = true
        hand.btn.visibility = View.INVISIBLE
        board.visibility = View.VISIBLE
        return hand
    }

    // 勝ち負けの判定を行うときに、出したカードを描画する関数
    private fun openField() {
        when(playerField?.rank) {
                'E' -> playerBoard.setImageResource(R.drawable.emperor)
                'C' -> playerBoard.setImageResource(R.drawable.citizen)
                'S' -> playerBoard.setImageResource(R.drawable.slave)
        }
        when(cpuField?.rank) {
            'E' -> cpuBoard.setImageResource(R.drawable.emperor)
            'C' -> cpuBoard.setImageResource(R.drawable.citizen)
            'S' -> cpuBoard.setImageResource(R.drawable.slave)
        }
    }

}
