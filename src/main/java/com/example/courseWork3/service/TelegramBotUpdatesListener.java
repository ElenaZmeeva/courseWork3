package com.example.courseWork3.service;

import com.example.courseWork3.model.Reminder;
import com.example.courseWork3.repositories.ReminderRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener{
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final TelegramBot telegramBot;

    private final ReminderRepository reminderRepository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, ReminderRepository reminderRepository1) {
        this.telegramBot = telegramBot;
        this.reminderRepository = reminderRepository1;
    }

    public static final String TEXT_REMIND = "([0-9.:\s]{16})(\\s)([\\W+][A-zА-я0-9_]+)";
    private final Pattern pattern= Pattern.compile(TEXT_REMIND);

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process (List<Update> updates) {
      updates.forEach(update -> {
          logger.info("Processing update: {}", update);
          TelegramBot bot= new TelegramBot(telegramBot.getToken());
          String message = update.message().text();
          long chatId = update.message().chat().id();
          String msg= "Привет, что тебе напомнить?";

          for (Update upd: updates){
              if (message.equals("/start")&& upd.message()!=null) {
                  SendResponse response = bot.execute(new SendMessage(chatId, msg));
              } else {
                  System.out.println("Message can't be sent");
              }

          }
          createRemind(update);
      });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private List<String> checkMessage(String message){
        LocalDateTime.parse("01.01.2022 20:00", DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        Matcher matcher= pattern.matcher(message);
        if(matcher.matches()){
            String date= matcher.group(1);
            String task_text= matcher.group(3);
            return List.of(date,task_text);
        }else {
            logger.warn("The symbols are not suitable", message);
            throw new IllegalStateException();
        }
    }
    public void createRemind(Update update){
        String message = update.message().text();
        long chatId = update.message().chat().id();
        checkMessage(message);
        Reminder reminder= new Reminder();
        String text= reminder.getTask_text();
        LocalDateTime time= reminder.getSendTime();
        Reminder reminders= new Reminder(1,chatId,text, time);
        reminderRepository.save(reminders);
logger.info("Reminder has been created", reminders);

    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendRemind(){
        List<Reminder>messageForRemind= reminderRepository.findAllBySendTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));

        messageForRemind.forEach(reminder -> {
          Long chat_id= reminder.getChat_id();
          telegramBot.execute(new SendMessage(chat_id, reminder.getTask_text()));
                });

    }
}

