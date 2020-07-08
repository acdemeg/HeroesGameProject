package com.neolab.heroesGame.server.networkServiceServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.client.networkService.Client;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.answers.AnswerProcessor;
import com.neolab.heroesGame.server.dto.ClientResponse;
import com.neolab.heroesGame.server.dto.ServerRequest;
import com.neolab.heroesGame.server.answers.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private final Map<Integer, Client> clients = new HashMap<>();

    public Server(){
        super();
        clients.put(1, new Client(1));
        clients.put(2, new Client(2));
        AnswerProcessor.setActivePlayer(clients.get(1).getPlayer());
        AnswerProcessor.setPlayer(clients.get(2).getPlayer());
    }

    /**
     * главный циккл игры
     * опрашиваем игроков по очереди
     * если у одного закончились доступные ходы
     * другой ходит подряд до конца текущего раунда
     */
    public void playingProcess() throws JsonProcessingException, HeroExceptions {
        Client[] arrClients = clients.values().toArray(new Client[2]);
        int endRes;

        while(true){
            for (int i = 1; i <= arrClients.length; i++) {
                if(i == 2) {
                    if(checkAvailableHero(arrClients[i].getPlayer().getId())){
                        askNextPlayer(i, arrClients);
                    }
                    i = 1;
                }
                else if(checkAvailableHero(arrClients[i].getPlayer().getId())) {
                    askNextPlayer(i, arrClients);
                }
                //в конце раунда доступны все живые герои
                if(checkEndRound()){
                    AnswerProcessor.getBoard().getArmies().values().forEach(Army::setAvailableHeroes);
                }
            }

            endRes = checkEndGame();
            if(endRes > 0){
                LOGGER.info(String.format("Пользователь c id = [%d] победил", endRes));
                return;
            }
        }
    }

    /**
     * спрашиваем следующего клиента
     * @param nextClient номер след. клиента
     * @param arrClients массив клиентов
     */
    private void askNextPlayer(int nextClient, Client[] arrClients) throws JsonProcessingException, HeroExceptions {
        ServerRequest serverRequest = new ServerRequest(AnswerProcessor.getBoard(), AnswerProcessor.getActionEffect());
        String answerJson = arrClients[nextClient].getClientAnswer(serverRequest.getBoardJson(), serverRequest.getActionEffectJson());
        ClientResponse clientResponse = new ClientResponse(answerJson);
        Answer answerClient = clientResponse.getAnswer();
        AnswerProcessor.handleAnswer(answerClient);
    }

    /**
     *
     * @return id победившего игрока
     */
    private int checkEndGame(){
        int playerId = AnswerProcessor.getPlayer().getId();

        if(AnswerProcessor.getBoard().getArmy(playerId).getHeroes().size() == 0) {
            return AnswerProcessor.getActivePlayer().getId();
        }
        return 0;
    }

    /**
     * проверка конца текущего раунда игры
     * @return если у обоих игроков нет доступных героев значить уже все походили
     * p.s. или уже все на доске погибли...
     */
    private boolean checkEndRound(){
        int countAvailableHero =  AnswerProcessor.getBoard().getArmies().values().stream().map(
                army -> army.getAvailableHero().size()).reduce(Integer::sum).get();
        return countAvailableHero == 0;
    }

    private boolean checkAvailableHero(int playerId){
        return AnswerProcessor.getBoard().getArmy(playerId).getAvailableHero().size() != 0;
    }
}
