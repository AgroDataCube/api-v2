/*
 * Copyright 2018 Wageningen Environmental Research
 *
 * For licensing information read the included LICENSE.txt file.
 *
 * Unless required by applicable law or agreed to in writing, this software
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.mail;

import nl.wur.agrodatacube.registry.AgroDataCubeRegistry;
import java.util.*;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.*;
import javax.mail.internet.*;

/**
 *
 * @author frank013
 */
public class SMTPClient {

    /**
     * Send a mail
     */
    ArrayList recipientsTO = null;
    ArrayList recipientsCC = null;
    ArrayList recipientsBCC = null;
    String senderAddress = null;
    String server = null;
    ArrayList<String> body = null;
    int port = 25;
    javax.mail.Message msg;
    ArrayList<String> filenames = null;

    /**
     * Create the smtp client and set its defaults.
     */
    public SMTPClient() {
        recipientsTO = new ArrayList();
        recipientsCC = new ArrayList();
        recipientsBCC = new ArrayList();
        //WENR specific defaults
        setServer(AgroDataCubeRegistry.getInstance().getSMTPServer());
        setSenderAddress(AgroDataCubeRegistry.getInstance().getEmailSender());
        body = new ArrayList<String>();
        filenames = new ArrayList<String>();
    }

    /**
     * Add line to body
     *
     * @param l
     */
    public void addLineToBody(String l) {
        body.add(l);
    }

    /**
     * Add lines to body
     * @param c
     */
    public void addLinesToBody(Collection c) {
        body.addAll(c);
    }

    /**
     * Clear everything
     */
    public void clear() {
        recipientsTO.clear();
        body.clear();
        recipientsBCC.clear();
        recipientsCC.clear();
        setSenderAddress(null);
    }

    /**
     * Add recipient
     *
     * @param newRecipient
     */
    public void addRecipient(String newRecipient) {
        recipientsTO.add(newRecipient);
    }

    /**
     *  Add recipients
     *
     * @param newRecipients
     */
    @Deprecated
    public void addRecipient(Collection newRecipients) {
        recipientsTO.add(newRecipients);
    }
    
    public void addRecipient(ArrayList<String> newRecipients) {
       if (newRecipients != null) {
            for (String p : newRecipients) {
                addRecipient(p);
            }
        }
    }

    /**
     *  Add recipient
     *
     * @param newRecipients
     */
    public void addRecipient(String[] newRecipients) {
        if (newRecipients != null) {
            for (String p : newRecipients) {
                addRecipient(p);
            }
        }
    }
    /**
     *  Add recipient to CC
     *
     * @param newRecipient
     */
    public void addCCRecipient(String newRecipient) {
        recipientsCC.add(newRecipient);
    }

    /**
     *  Add recipients to CC
     *
     * @param newRecipients
     */
    public void addBCCRecipient(Collection newRecipients) {
        recipientsBCC.add(newRecipients);
    }

    /**
     *  Add recipient to BCC
     *
     * @param newRecipient
     */
    public void addBCCRecipient(String newRecipient) {
        recipientsBCC.add(newRecipient);
    }

    /**
     *  Add recipients to BCC
     *
     * @param newRecipients
     */
    @Deprecated
    public void addCCRecipient(Collection newRecipients) {
        recipientsCC.add(newRecipients);
    }

    public void addFile(String name) {
        filenames.add(name);
    }

    /**
     * Which smtpserver to use
     * @param newServer
     */
    public void setServer(String newServer) {
        server = newServer;
    }

    /**
     * Return name smtp server
     *
     * @return 
     */
    public String getServer() {
        return server;
    }

    /**
     * Set sender
     *
     * @param newSenderAddress Emailaddress sender.
     */
    public void setSenderAddress(String newSenderAddress) {
        senderAddress = newSenderAddress;
    }

    /**
     * Convert all addresses from strings to InternetAdresses.
     * @param al
     * @return
     * @throws javax.mail.internet.AddressException 
     */
    private InternetAddress[] convertAddresses(ArrayList al) throws javax.mail.internet.AddressException {
        InternetAddress[] addressTo = new InternetAddress[al.size()];
        for (int i = 0; i < al.size(); i++) {
            String s = (String) al.get(i);
            addressTo[i] = new InternetAddress(s);
        }
        return addressTo;
    }

    /**
     * Send the message.
     * 
     * @throws MessagingException 
     */
    protected void doSend() throws MessagingException {
        Transport.send(msg);
    }

    /**
     * Send the mail, we do this in a separate thread to allow program execution to continue and not wait for result of mail 
     * sending. This can lead to mails not being send and not detect tha. We accept that.
     * @param subject 
     * @param message 
     * @throws java.lang.Exception
     */
    public void sendMessage(String subject, String message) throws Exception {
        {
            //
            // See if we have all we need
            //

            if (senderAddress == null) {
                throw new Exception("No sender");
            }

            if (recipientsTO.isEmpty()) {
                throw new Exception("No recipients (BCC or CC)");
            }

            //Set the host smtp address
            Properties props = new Properties();
            props.put("mail.smtp.host", getServer());

            // create some properties and get the default Session
            Session session = Session.getDefaultInstance(props, null);

            // create a message
            msg = new MimeMessage(session);

            // set the from and to address
            InternetAddress addressFrom = new InternetAddress(senderAddress);
            msg.setFrom(addressFrom);

            msg.setRecipients(Message.RecipientType.TO, convertAddresses(recipientsTO));
            msg.setRecipients(Message.RecipientType.CC, convertAddresses(recipientsCC));
            msg.setRecipients(Message.RecipientType.BCC, convertAddresses(recipientsBCC));

            // Optional : You can also set your custom headers in the Email if you Want
            // msg.addHeader("MyHeaderName", "myHeaderValue");
            // Setting the Subject and Content Type
            msg.setContent(message, "text/plain");

            if (filenames.size() > 0) {
                Multipart multipart = new MimeMultipart();
                for (String s : filenames) {
                    MimeBodyPart messageBodyPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(s);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(s);
                    multipart.addBodyPart(messageBodyPart);
                }
                msg.setContent(multipart);
                filenames.clear();
            }

            msg.setSubject(subject);
            
            /**
             * IT is complete sO send in a separate thread since some smtp servers are a bit slow
             */
            new MailSenderThread(msg).start();
        }
    }

}

/**
 * This class sends the email and does that asynchronously. The caller does not need to wait. 
 *
 * @author Rande001
 */
class MailSenderThread extends Thread {

    Message message = null;

    public MailSenderThread(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            Transport.send(message);
        } catch (Exception e) {
            System.out.println("Sending mail : " + e.getMessage());
        }
    }
}
