package com.example.kidsgames.core

import java.util.Locale

/** The three languages the app teaches. Portuguese defaults to pt-BR. */
val EN: Locale = Locale.ENGLISH
val PL: Locale = Locale("pl")
val PT: Locale = Locale("pt", "BR")

/** A short word or phrase in English / Polish / Portuguese, with an optional emoji. */
data class Word(
    val en: String,
    val pl: String,
    val pt: String,
    val emoji: String = "",
)

/** The three spoken forms of this word, in EN → PL → PT order. */
fun Word.utterances(): List<SpeechService.Utterance> = listOf(
    SpeechService.Utterance(en, EN),
    SpeechService.Utterance(pl, PL),
    SpeechService.Utterance(pt, PT),
)

/** Speak a [Word] in English → Polish → Portuguese. */
fun SpeechService.say(word: Word) = speakSequence(word.utterances())

/** Speak a [Word] in just one randomly chosen language (fast, no three-language delay). */
fun SpeechService.sayOne(word: Word) {
    val (locale, text) = listOf(EN to word.en, PL to word.pl, PT to word.pt).random()
    speak(text, locale)
}

/** Encouraging phrases spoken after a correct answer or a win. */
object Phrases {
    val praise: List<Word> = listOf(
        Word("Well done!", "Brawo!", "Muito bem!"),
        Word("Great job!", "Świetnie!", "Muito bom!"),
        Word("Yes!", "Tak!", "Isso!"),
        Word("You did it!", "Udało się!", "Você conseguiu!"),
    )

    val youDidIt = Word("You did it!", "Udało się!", "Você conseguiu!")

    /** A rotating cheer, so it does not always say the same thing. */
    fun cheer(step: Int): Word = praise[((step % praise.size) + praise.size) % praise.size]
}
