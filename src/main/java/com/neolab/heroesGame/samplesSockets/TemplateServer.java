package com.neolab.heroesGame.samplesSockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Консольный многопользовательский чат.
 * Сервер
 */
public class TemplateServer extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateServer.class);

    static final public int PORT = 8083;
    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final int COUNT_EVENTS_IN_HISTORY = 20;

    enum Command {
        WARNING("warning"),
        STOP_CLIENT_FROM_SERVER("stop client from server"),
        STOP_CLIENT("stop client"),
        STOP_ALL_CLIENTS("stop all clients"),
        STOP_SERVER("stop server"),
        ;

        private final String commandName;

        Command(final String commandName) {
            this.commandName = commandName;
        }

        boolean equalCommand(final String message) {
            return commandName.equals(message);
        }

        static boolean isCommandMessage(final String message) {
            for (final Command command : values()) {
                if (command.equalCommand(message)) {
                    return true;
                }
            }
            return false;
        }
    }

    private final ConcurrentLinkedQueue<ServerSomething> serverList = new ConcurrentLinkedQueue<>();
    private History history = new History(); // история

    private class ServerSomething extends Thread {

        private final TemplateServer server;
        private final Socket socket;
        private final BufferedReader in; // поток чтения из сокета
        private final BufferedWriter out; // поток завписи в сокет
        private String nickName = null;

        /**
         * Для общения с клиентом необходим сокет (адресные данные)
         *
         * @param server сервер
         * @param socket сокет
         */
        private ServerSomething(final TemplateServer server, final Socket socket) throws IOException {
            this.server = server;
            this.socket = socket;
            // если потоку ввода/вывода приведут к генерированию искдючения, оно проброситься дальше
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }

        @Override
        public void run() {
            try {
                // первое сообщение отправленное сюда - это nickName
                nickName = in.readLine();
                for (final ServerSomething ss : server.serverList) {
                    if (!ss.nickName.equals(nickName)) {
                        continue;
                    }
                    processDuplicatedNickName();
                    return;
                }
                processUniqueNickName();
                while (true) {
                    if (!processMessage()) {
                        break;
                    }
                }
            } catch (final IOException e) {
                LOGGER.error(String.format("Соединение с пользователем [%s] будет разорвано. Возникла ошибка: ",
                        this.nickName, e.getMessage()));
                this.downService();
            }
        }

        /**
         * Обработать сообщение
         *
         * @return {@code false} окончить работу после обработки сообщения, иначе {@code true}
         * @throws IOException ошибка ввода-вывода
         */
        private boolean processMessage() throws IOException {
            final String message = in.readLine();
            final String preparedMessage = getPreparedMessage(message);
            sendMessage(preparedMessage);
            if (Command.STOP_CLIENT.equalCommand(message)) {
                LOGGER.info(String.format("Пользователь [%s] запрос разрыв соединения", this.nickName));
                downService();
                return false;
            } else if (Command.STOP_ALL_CLIENTS.equalCommand(message) || Command.STOP_SERVER.equalCommand(message)) {
                LOGGER.info(String.format("Пользователь [%s] отправил команду %s", this.nickName, message));
                for (final ServerSomething ss : server.serverList) {
                    ss.send(message);
                    ss.downService();
                }
                if (Command.STOP_SERVER.equalCommand(message)) {
                    System.exit(0);
                }
                return false;
            }
            return true;
        }

        private void sendMessage(final String message) throws IOException {
            System.out.println(message);
            server.history.addHistoryEvent(message);
            for (final ServerSomething ss : server.serverList) {
                ss.send(message);
            }
        }

        private String getPreparedMessage(final String message) {
            final String preparedMessage;
            if (Command.WARNING.equalCommand(message)) {
                preparedMessage = formatMessage("Warning from " + formatNickName(nickName));
            } else if (Command.STOP_CLIENT.equalCommand(message)) {
                preparedMessage = formatMessage("Disconnect " + formatNickName(nickName));
            } else if (Command.STOP_ALL_CLIENTS.equalCommand(message)) {
                preparedMessage = formatMessage("Stop all clients from " + formatNickName(nickName));
            } else if (Command.STOP_SERVER.equalCommand(message)) {
                preparedMessage = formatMessage("Stop server from " + formatNickName(nickName));
            } else {
                preparedMessage = message;
            }
            return preparedMessage;
        }

        private void processDuplicatedNickName() throws IOException {
            final String duplicatedNickName = formatNickName(nickName + " (duplicated)");
            final String duplicatedMessage = formatMessage(String.format("%s disconnected", duplicatedNickName));
            sendMessage(duplicatedMessage);
            send(duplicatedMessage);
            send(formatCommandMessage(Command.STOP_CLIENT_FROM_SERVER.commandName, duplicatedNickName));
            send(Command.STOP_CLIENT_FROM_SERVER.commandName);
            LOGGER.info("Имя пользователя дубилроваллось. Отправлен запрос пользователю на прекращение работы.");
            downService();
        }

        private void processUniqueNickName() throws IOException {
            serverList.add(this);
            server.history.printStory(out);
            final String connectMessage = formatMessage("Connect " + formatNickName(nickName));
            sendMessage(connectMessage);
            LOGGER.info("Пользователю [%s] отправлена история сообщений");
        }

        /**
         * отсылка одного сообщения клиенту
         *
         * @param msg сообщение
         */
        private void send(final String msg) throws IOException {
            out.write(msg + "\n");
            out.flush();
        }

        private String formatMessage(final String message) {
            final Date date = new Date();
            final String strTime = DATE_FORMAT.format(date);
            return String.format("[%s] %s", strTime, message);
        }

        private String formatCommandMessage(final String message, final String nickName) {
            return String.format("%s [command] to %s", formatMessage(message), nickName);
        }

        private String formatNickName(final String nickName) {
            return String.format("[%s]", nickName);
        }

        /**
         * закрытие сервера, удаление себя из списка нитей
         */
        private void downService() {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                    in.close();
                    out.close();
                    server.serverList.remove(this);
                    LOGGER.info(String.format("Соединение с пользователем [%s] успешно разорвано"), this.nickName);
                }
            } catch (final IOException ignored) {
            }
        }
    }

    /**
     * класс хранящий в ссылочном приватном
     * списке информацию о последних сообщениях
     */
    private class History {

        private final ConcurrentLinkedDeque<String> events = new ConcurrentLinkedDeque<>();

        /**
         * добавить новый элемент в список
         *
         * @param event элемент
         */
        private void addHistoryEvent(final String event) {
            if (events.size() >= COUNT_EVENTS_IN_HISTORY) {
                events.removeFirst();
            }
            events.add(event);
        }

        /**
         * отсылаем последовательно каждое сообщение из списка
         * в поток вывода данному клиенту (новому подключению)
         *
         * @param writer поток вывода
         */
        private void printStory(final BufferedWriter writer) {
            if (events.size() > 0) {
                try {
                    writer.write("History messages\n");
                    for (final String event : events) {
                        writer.write(event + "\n");
                    }
                    writer.write("...\n");
                    writer.flush();
                } catch (final IOException ignored) {
                }
            }
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void startServer() throws IOException {
        history = new History();
        LOGGER.info(String.format("Server started, port: %d", PORT));
        try (final ServerSocket serverSocket = new ServerSocket(PORT)) {
            // serverSocket.setSoTimeout(1000);
            while (true) { // приложение с помощью System.exit() закрывается по команде от клиента
                // Блокируется до возникновения нового соединения
                final Socket socket = serverSocket.accept();
                try {
                    new ServerSomething(this, socket).start();
                } catch (final IOException e) {
                    // Если завершится неудачей, закрывается сокет,
                    // в противном случае, нить закроет его:
                    socket.close();
                    LOGGER.error("Не удалось установить подключение с новым пользователем. Возникла ошибка: "
                            + e.getMessage());
                }
            }
        } catch (final BindException e) {
            LOGGER.error("Работа сервера будет прекращена. Возникла ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}