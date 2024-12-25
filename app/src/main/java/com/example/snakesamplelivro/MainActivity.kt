package com.example.snakesamplelivro

import android.R.attr.value
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.snakesamplelivro.ui.theme.SnakeSampleLivroTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val game = Game(lifecycleScope)
        enableEdgeToEdge()
        setContent {
            SnakeSampleLivroTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    innerPadding ->
                    JogoCobrinha(game)
                }
            }
        }
    }
}


data class EstadoJogo(val comida:Pair <Int,Int>,val cobra:List<Pair<Int,Int>>)

class Game(private val scope: CoroutineScope){
    private val mutex: Mutex= Mutex()
    private val mutableState : MutableStateFlow<EstadoJogo> =
        MutableStateFlow(EstadoJogo(comida=Pair(5, 5),cobra = listOf( Pair(7, 7))))
    val estadoJogo: Flow<EstadoJogo> = mutableState

    var movimento = Pair(1, 0)
        set(value) {
            scope.launch {
                mutex.withLock {
                    field = value
                }
            }
        }



    init {
        scope.launch {
            var tamanhoCobra = 4
            while (true){
                delay(500)
                mutableState.update {
                    val newPosition : Pair<Int,Int> = it.cobra.first().let {
                            poz:Pair<Int, Int> ->
                        mutex.withLock {Pair(
                            (poz.first+movimento.first+ BOARD_SIZE)% BOARD_SIZE,
                            (poz.second+movimento.second+BOARD_SIZE)%BOARD_SIZE
                        )
                        }
                    }
                    if(newPosition == it.comida){
                        tamanhoCobra++
                    }
                    if(it.cobra.contains(newPosition)){
                        tamanhoCobra = 4
                    }


                    it.copy(
                        comida = if(newPosition == it.comida){
                            Pair(
                                (0 until BOARD_SIZE).random(),
                                (0 until BOARD_SIZE).random()
                            )
                        }else
                            it.comida,
                        cobra = listOf(newPosition)+it.cobra.take(tamanhoCobra-1)
                    )
                }


            }
        }
    }
    companion object{
        const val BOARD_SIZE = 16
    }
}

@Composable
fun JogoCobrinha(game: Game){
    val estadoJogo = game.estadoJogo.collectAsState(initial = null)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        estadoJogo.value?.let {
            Campo(it)
        }
        Botoes{
            game.movimento = it
        }
    }
}
@Composable
fun Botoes(onDirectionChange: (Pair<Int, Int>) -> Unit){
    val tamBotaoMod = Modifier.size(64.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)) {
        Button(
            onClick = { onDirectionChange(Pair(0, -1)) },
            modifier = tamBotaoMod
        ) {
            Text("Cima")
        }
        Row{
            Button(onClick = { onDirectionChange(Pair(-1, 0))},
                modifier = tamBotaoMod
            ) {
                Text("Esquerda")
            }
            Spacer(modifier=tamBotaoMod)
            Button(
                onClick = { onDirectionChange(Pair(1, 0))},
                modifier = tamBotaoMod
            ) {
                Text("Direita")
            }
        }
        Button(
            onClick = { onDirectionChange(Pair(0, 1))},
            modifier = tamBotaoMod
        ) {
            Text("Baixo")
        }


    }

}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Campo(estadoJogo: EstadoJogo){
    BoxWithConstraints (Modifier.padding(16.dp)){
        val tileSize = maxWidth/Game.BOARD_SIZE
        Box(Modifier
            .size(maxWidth)
            .border(2.dp, Color.Green)){
        }
        Box(
            Modifier
                .offset(x = tileSize * estadoJogo.comida.first, y = tileSize * estadoJogo.comida.second)
                .size(tileSize)
                .background(Color.DarkGray, CircleShape)
        )
        estadoJogo.cobra.forEach { cobra ->
            Box(
                modifier = Modifier
                    .offset(x = tileSize * cobra.first, y = tileSize * cobra.second)
                    .size(tileSize)
                    .background(
                        Color.DarkGray,
                        Shapes().small
                    )
            )

        }
    }
}





@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SnakeSampleLivroTheme {

    }
}

