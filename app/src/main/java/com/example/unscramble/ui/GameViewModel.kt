package com.example.unscramble.ui
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.ui.GameUiState
import kotlinx.coroutines.flow.update

class GameViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState:StateFlow<GameUiState> = _uiState.asStateFlow()
    private var usedWords: MutableSet<String> = mutableSetOf()
    private lateinit var currentWord: String
    var userGuess by mutableStateOf("")
        private set

    //pick random word and shuffle it
    private fun pickRandomWordAndShuffle(): String{
        currentWord = allWords.random()
        if(usedWords.contains(currentWord)){
            return pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    //shuffle word
        private fun shuffleCurrentWord(word : String): String{
        val tempWord = word.toCharArray()
        tempWord.shuffle()
        while(String(tempWord).equals(word)){
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    //
    fun resetGame(){
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambleWord = pickRandomWordAndShuffle())
    }
    init {
        resetGame()
    }

    //update user guess
    fun updateUserGuess(guessedWord: String){
        userGuess = guessedWord
    }

    //valider le mot et modifier score
    fun checkUserGuess(){
        if(userGuess.equals(currentWord, ignoreCase = true)){
            //increase score
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        }else{
            //user's guess wrong, show an error
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        //reset user guess
        updateUserGuess("")
    }

    private fun updateGameState(updatedScore: Int){
        _uiState.update{ currentState ->
            currentState.copy(
                isGuessedWordWrong = false,
                score = updatedScore,
                currentScrambleWord = pickRandomWordAndShuffle(),
                currentWordCount = currentState.currentWordCount.inc(),
            )
        }
    }

    //methode pour passer un mot
    fun skipWord(){
        updateGameState(_uiState.value.score)
        updateUserGuess("")
    }
}