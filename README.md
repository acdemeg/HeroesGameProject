# Heroes Game 
 Репозиторий содержит игру "Герои" которая представляет из себе аватарку
 популярной игры Disciples. Играть могут как люди так и боты, также бот против 
 человека и бот против бота, бот играет не оптимально(делает рандомные ходы из доступных).
 ![](./Board_horizontal.png?raw=true "Optional Title")
 
 Игра сделана как клиент-сервер, отдельно запускается Server и отдельно четное кол-во 
 клиентов(Client) - пакет [simpleSockets](./src/main/java/com/neolab/heroesGame/simplesSockets).
 Клиенты распределяются в игровые комнаты(GameRoom) по 2 в каждую, настройки сервера игры 
 такие как макс. кол-во игроков, макс. кол-во комнат, макс.время ответа и т.д. содержатся 
 в server.properties файле. В пакете [additional](./src/main/java/com/neolab/heroesGame/aditional) 
 содержатся доп. модули для сбора и анализ статистики игр между игроками/ботами. 
 Диаграмма классов(uml) проекта - [heroesGameDiagram](./heroesGameDiagram.png).
 
 [Более подробное описание правил игры](https://docs.google.com/document/d/1VHH-vOE5kni4t4y9J9IlvloM_B6WdYzfO0f5_xq5BrQ/edit?usp=sharing)
 <br /><br />
 Презентация с описанием игры
 
 [![](./screenshots/preview.png?raw=true)](https://docs.google.com/presentation/d/1-LUNPJIff80EA9H0CB1v_pIzxnepgFvsuUC_Ezn2u6Y/edit?usp=sharing)
 
 Для игры есть "умный бот" который содержит ИИ реализующее 2 игровые 
 стратегии - https://github.com/acdemeg/Hero_Game_Project_SmartBot .
 
 # Как запускать
 Вариант 1
 1. `В пакете simpleSockets запустить Server`
 2. `Запустить Client, ввести имя и выбрать режим`
 3. `В конфигурация запуска должно быть разрешение на параллельный запуск Client, запускаем второго и последующего клиентов` 

 Вариант 2 (С "умным ботом")
  1. `Собрать проект mvn package`
  2. `Запустить Heroes_Game-1.0.jar`
  3. `Собрать проект Hero_Game_Project_SmartBot`
  4. `Запустить Heroes_Game-1.0.jar внутри Hero_Game_Project_SmartBot`
  5. `Либо запустить Client внутри Heroes_Game`

# Технологии
Игра была создана с использованием Java 11+, jUnit 4, Swing, 
Jackson Framework, Maven, Opencsv, Lanterna, Slf4J, Logback, JMathPlot.
  
# Архитектура
![](./heroesGameDiagram.png?raw=true "Optional Title")
  
# Скриншоты
Начало игры
![](./screenshots/screen-1.png?raw=true "Optional Title")
<br /><br />
Процесс игры
![](./screenshots/screen-2.png?raw=true "Optional Title")
<br /><br />
Пример лога игры
![](./screenshots/screen-3.png?raw=true "Optional Title")
<br /><br />
Пример файла статистики игр
![](./screenshots/screen-4.png?raw=true "Optional Title")
