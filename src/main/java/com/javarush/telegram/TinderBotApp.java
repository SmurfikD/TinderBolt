package com.javarush.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBotApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "test_tinder_a_i_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "Bot"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = "GPT"; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    private ArrayList<String> list = new ArrayList<>();

    private UserInfo me;
    private UserInfo friend;
    private int questionCount;

    public TinderBotApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь

        String message = getMessageText();
        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);

            showMainMenu(
                    "Главное меню бота", "/start",
                    "Генерация Tinder-профля \uD83D\uDE0E", "/profile",
                    "Сообщение для знакомства \uD83E\uDD70", " /opener",
                    "Переписка от вашего имени \uD83D\uDE08", " /message",
                    "Переписка со звездами \uD83D\uDD25", "/date",
                    "Задать вопрос чату GPT \uD83E\uDDE0", "/gpt");
            return;
        }


        //Command Chat GPT
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if (currentMode == DialogMode.GPT && isMessageCommand()) {
            String prompt = loadPrompt("gpt");
            Message msg = sendTextMessage("Подождите, чат GPt \uD83E\uDDE0 думает...");
            String answer = chatGPT.sendMessage(prompt, message);
            updateTextMessage(msg, answer);
            return;
        }


        //Command DATE
        if (message.equals("/date")) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(text,
                    "Ариана Гранде", "date_grande",
                    "Марго Робби", "date_robbie",
                    "Зендея", "date_zendeya",
                    "Райн Гослинг", "date_gosling",
                    "Том Харди", "date_hardy");
            return;
        }

        if (currentMode == DialogMode.DATE && isMessageCommand()) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage(" Отличный выбор!" +
                        "\nТвоя задача пригласить звезду на свидание " +
                        "❤\uFE0F за 5 сообщений");
                String prompt = loadPrompt(query);
                chatGPT.setPrompt(prompt);
                return;
            }
            Message msg = sendTextMessage("Подождите, собеседник набирает текст...");
            String answer = chatGPT.addMessage(message);
            updateTextMessage(msg, answer);
            return;
        }


        //command MESSAGE
        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Пришлите в чат вашу переписку",
                    "Следующее сообщение", "message_next",
                    "Пригласить на свидание", "message_date");
            return;
        }
        if (currentMode == DialogMode.MESSAGE && isMessageCommand()) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                String prompt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);
                Message msg = sendTextMessage("Подождите, чат GPt \uD83E\uDDE0 думает...");
                String answer = chatGPT.sendMessage(prompt, userChatHistory);
                updateTextMessage(msg, answer);
                return;
            }

            list.add(message);
            return;
        }

        //command PROFILE
        if (message.equals("/profile")) {
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");
            me = new UserInfo();
            sendTextMessage("Давайте ответим на несколько вопросов" +
                    " и заполним анкету");

            questionCount = 1;
            sendTextMessage("Как Вас зовут?");
            return;
        }

        if (currentMode == DialogMode.PROFILE && isMessageCommand()) {
            switch (questionCount) {
                case 1 -> {
                    me.name = message;
                    questionCount = 2;
                    sendTextMessage("Сколько вам лет?");
                    return;
                }
                case 2 -> {
                    me.age = message;
                    questionCount = 3;
                    sendTextMessage("Ваш пол?");
                    return;
                }
                case 3 -> {
                    me.sex = message;
                    questionCount = 4;
                    sendTextMessage("Из какого Вы города?");
                    return;
                }
                case 4 -> {
                    me.city = message;
                    questionCount = 5;
                    sendTextMessage("Кем вы работаете?");
                    return;
                }
                case 5 -> {
                    me.occupation = message;
                    questionCount = 6;
                    sendTextMessage("У Вас есть хобби?");
                    return;
                }
                case 6 -> {
                    me.hobby = message;
                    questionCount = 7;
                    sendTextMessage("Занимаетесь ли Вы спортом и каким?");
                    return;
                }
                case 7 -> {
                    me.handsome = message;
                    questionCount = 8;
                    sendTextMessage("Ваше финансовое состояние");
                    return;
                }
                case 8 -> {
                    me.wealth = message;
                    questionCount = 9;
                    sendTextMessage("Что Вам Не нравится в людях?");
                    return;
                }
                case 9 -> {
                    me.annoys = message;
                    questionCount = 10;
                    sendTextMessage("Цель знакомства?");
                    return;
                }
                case 10 -> {
                    me.goals = message;
                    String aboutMyself = me.toString();
                    String prompt = loadPrompt("profile");
                    Message msg = sendTextMessage("Подождите, чат GPt \uD83E\uDDE0 думает...");
                    String answer = chatGPT.sendMessage(prompt, aboutMyself);
                    updateTextMessage(msg, answer);
                    return;
                }
            }

            return;
        }

        //command OPENER
        if (message.equals("/opener")) {
            currentMode = DialogMode.OPENER;
            sendPhotoMessage("opener");
            friend = new UserInfo();
            sendTextMessage("Давайте ответим на несколько вопросов" +
                    " и заполним анкету того, " +
                    "с кем Вы хотите познакомиться");
            questionCount = 1;
            sendTextMessage("Как его/ее зовут?");
            return;
        }

        if (currentMode == DialogMode.OPENER && isMessageCommand()) {
            switch (questionCount) {
                case 1 -> {
                    friend.name = message;
                    questionCount = 2;
                    sendTextMessage("Сколько ему/ей лет?");
                    return;
                }
                case 2 -> {
                    friend.age = message;
                    questionCount = 3;
                    sendTextMessage("Пол?");
                    return;
                }
                case 3 -> {
                    friend.sex = message;
                    questionCount = 4;
                    sendTextMessage("Из какого города?");
                    return;
                }
                case 4 -> {
                    friend.city = message;
                    questionCount = 5;
                    sendTextMessage("Кем работает?");
                    return;
                }
                case 5 -> {
                    friend.occupation = message;
                    questionCount = 6;
                    sendTextMessage("Есть ли хобби?");
                    return;
                }
                case 6 -> {
                    friend.hobby = message;
                    questionCount = 7;
                    sendTextMessage("Занимается ли спортом и каким?");
                    return;
                }
                case 7 -> {
                    friend.handsome = message;
                    questionCount = 8;
                    sendTextMessage("Финансовое состояние");
                    return;
                }
                case 8 -> {
                    friend.wealth = message;
                    questionCount = 9;
                    sendTextMessage("Что НЕ нравится в людях?");
                    return;
                }
                case 9 -> {
                    friend.annoys = message;
                    questionCount = 10;
                    sendTextMessage("Цель знакомства?");
                    return;
                }
                case 10 -> {
                    friend.goals = message;
                    String aboutFriend = friend.toString();
                    String prompt = loadPrompt("opener");
                    Message msg = sendTextMessage("Подождите, чат GPt \uD83E\uDDE0 думает...");
                    String answer = chatGPT.sendMessage(prompt, aboutFriend);
                    updateTextMessage(msg, answer);
                    return;
                }
            }
        }

    }
//        sendTextMessage("Вы написали" + message);
//        sendTextMessage("_X_"); курсив
//        sendTextMessage("*X*"); жирный текст
//        sendTextButtonsMessage("Выберите режим работы","Старт", "start");




    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBotApp());
    }
}
