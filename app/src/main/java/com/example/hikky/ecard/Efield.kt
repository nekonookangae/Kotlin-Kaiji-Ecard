package com.example.hikky.ecard

import android.widget.ImageButton

// 場のフィールド(カード)を表すクラス
/*
   @param rank       カードの種類
   @param btn        フィールドとrankに対応する画像を合わせたImageButton
   @param isReverse  カードの表裏を判別するBool値。相手の手札が見えないようにするため
   @param isRemove   カードが既に場に出ているか

 */
class Efield(val rank: Char, val btn: ImageButton,
            private var isReverse: Boolean = false, var isRemove: Boolean = false) {

    // 初期化時に、カードの種類・表裏に応じた画像をボタンに描画する
    init {
        drawCard()
    }

    // カードの表裏をもとに画像に反映させる
    private fun drawCard() {
        if (isReverse) {
            btn.setImageResource(R.drawable.reverse_inv)
        }
        else {
            when(rank) {
                'E' -> btn.setImageResource(R.drawable.emperor)
                'C' -> btn.setImageResource(R.drawable.citizen)
                'S' -> btn.setImageResource(R.drawable.slave)
            }
        }
    }

    // 自分のカードと引数にとったカードの階級を比べ、勝敗(-1:負, 0:引分, 1:勝)を返す
    fun compareRank(opsRank: Char?): Int {
        return when (rank) {
            'E' -> if (opsRank == 'C') 1 else -1
            'C' -> when (opsRank) {
                'E' ->  -1
                'C' -> 0
                else -> 1
            }
            else -> if (opsRank == 'E') 1 else -1
        }
    }

}