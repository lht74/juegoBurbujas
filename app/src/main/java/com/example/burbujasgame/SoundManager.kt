package com.example.burbujasgame

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.annotation.RawRes

object SoundManager {
    private var musicPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()

    fun init(context: Context) {
        soundPool = SoundPool.Builder().setMaxStreams(5).build()
        soundMap["good"] = soundPool!!.load(context, R.raw.good_bubble, 1)
        soundMap["bad"] = soundPool!!.load(context, R.raw.bad_bubble, 1)
        soundMap["missing"] = soundPool!!.load(context, R.raw.missing_bubble, 1)
    }

    fun playEffect(key: String) {
        soundMap[key]?.let { soundId ->
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }
    fun playMissing() {
        playEffect("missing")
    }

    fun startMusic(context: Context, @RawRes musicRes: Int) {
        stopMusic()
        musicPlayer = MediaPlayer.create(context, musicRes)
        musicPlayer?.isLooping = true
        musicPlayer?.start()
    }

    fun stopMusic() {
        musicPlayer?.stop()
        musicPlayer?.release()
        musicPlayer = null
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        stopMusic()
    }
}
