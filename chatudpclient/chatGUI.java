/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatudpclient;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author alessia
 */
public class chatGUI extends JFrame implements Runnable {

    //Classi che saranno necessarie per la comunicazione
    private static DatagramSocket socket;
    private static String IP_address;
    private static InetAddress address;
    private static int UDP_port;
    private String username;

    //Dichiarazione degli elementi del JFrame
    //Pannelli
    JPanel ContenutoInvio = new JPanel();

    //Menù
    JMenuBar menu = new JMenuBar();
    JMenu Gestione_chat = new JMenu("Opzioni Chat");
    JMenuItem InserimentoIP_Server = new JMenuItem("Inserisci Indirizzo IP Server");
    JMenuItem AggiungiUser = new JMenuItem("Aggiungi Username");

    //Area della Chat
    private static JTextArea areaChat = new JTextArea();
    JScrollPane scrollPane = new JScrollPane(areaChat); //Aggiungo uno scrollPane per avere la possibilità di scrollare la chat

    //Area Invio
    JTextField messaggio = new JTextField("Scrivi il tuo messaggio qui");
    JButton invia = new JButton("Invia");

    public chatGUI() throws InterruptedException {

        //Configurazione del JFrame
        this.setTitle("Chat di gruppo");
        this.setSize(600, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);//Dimensione Frame non modificabile
        this.setLayout(new GridLayout(2, 1));
        //Configurazione JFrame

        //Configurazione menù
        menu.add(Gestione_chat);
        Gestione_chat.add(InserimentoIP_Server);
        Gestione_chat.add(AggiungiUser);
        this.setJMenuBar(menu);
        //Configurazione menù

        //Configurazione Pannelli
        ContenutoInvio.setLayout(new GridLayout(1, 2));
        //Padding per l'area Invio
        ContenutoInvio.setBorder(new EmptyBorder(60, 20, 60, 20));
        //Padding per l'area messaggio
        messaggio.setBorder(new EmptyBorder(10, 10, 10, 10));
        ContenutoInvio.add(messaggio);
        ContenutoInvio.add(invia);
        //La TextArea in cui è presente la chat non è modificabile
        areaChat.setEditable(false);
        //Padding per l'area messaggio
        areaChat.setBorder(new EmptyBorder(20, 20, 20, 20));
        Font fontArea = null;
        //Impostazione Font
        areaChat.setFont(fontArea);
        //Rendo sempre visualizzabile la barra di scroll verticale
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(scrollPane);
        this.add(ContenutoInvio);
        //Configurazione Pannelli

        //FocusListener per il campo messaggio (se seleziono l'area per scrivere un messaggio, il placheholder viene cancellato)
        messaggio.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                //Svuoto il TextField
                messaggio.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });

        //ActionListener Bottone Invia
        invia.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (username == null && IP_address == null) {
                    //Controllo che l'username e l'IP del server siano stati inseriti
                    JOptionPane.showMessageDialog(null, "Username o Indirizzo IP del server non inseriti correttamente!");
                } else {
                    //Se tutto è ok, il pacchetto viene inviato
                    inviaPacchetto(messaggio.getText(), username);
                    //Svuoto il campo messaggio dopo l'invio
                    messaggio.setText("");
                }

            }
        });

        //ActionListener InserisciIPServer
        InserimentoIP_Server.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //OptionPane per l'input dell'utente
                IP_address = JOptionPane.showInputDialog("Inserisci l'indirizzo IP del Server: ");
                if (IP_address != null) {
                    //Controllo che l'IP sia stato inserito
                    setTitle("Chat di gruppo [" + "Username: " + username + " -  IP Server: " + IP_address + "]");
                    //Messaggio per l'utente
                    areaChat.append("Indirizzo IP " + "''" + IP_address + "''" + " inserito correttamente!");
                    areaChat.append("\n");
                }
            }
        });

        //ActionListener AggiungiUsername
        AggiungiUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //OptionPane per l'input dell'utente
                username = JOptionPane.showInputDialog("Inserisci il tuo username: ");
                if (username != null) {
                    //Controllo che l'username sia stato inserito
                    setTitle("Chat di gruppo [" + "Username: " + username + " -  IP Server: " + IP_address + "]");
                    //Messaggio per l'utente
                    areaChat.append("Username " + "''" + username + "''" + " inserito correttamente!");
                    areaChat.append("\n");
                }
            }
        });

        //Thread che rimane in ascolto e avvia la classe "riceviPacchetto" per ricevere pacchetti (Da suggerimento)
        Thread ascolta = new Thread() {
            public void run() {
                riceviPacchetto();
            }
        };
        ascolta.start(); //Avvio Thread
        
    }

    public static void inviaPacchetto(String messaggio, String username) {
        byte[] buffer;
        DatagramPacket userDatagram;

        //Ora del messaggio inviato che concateno con il messaggio dell'utente
        Date data = new Date();

        try {
            //Concateno l'username e l'ora con il messaggio(questa fase si ripete per ogni messaggio)
            messaggio = username.concat(messaggio);
            //Trasformo in array di byte la stringa che voglio inviare
            buffer = messaggio.getBytes("UTF-8");

            // Costruisco il datagram (pacchetto UDP) di richiesta 
            // specificando indirizzo e porta del server a cui mi voglio collegare
            // e il messaggio da inviare che a questo punto si trova nel buffer
            userDatagram = new DatagramPacket(buffer, buffer.length, address, UDP_port);
            // spedisco il datagram
            socket.send(userDatagram);
        } catch (IOException ex) {
            Logger.getLogger(chatGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void riceviPacchetto() {
        byte[] buffer = new byte[100];
        String received;
        DatagramPacket serverDatagram;

        try {
            // Costruisco il datagram per ricevere i pacchetti inviati dal server
            serverDatagram = new DatagramPacket(buffer, buffer.length);
            // fino a quando il main non interrompe il thread rimango in ascolto 
            while (!Thread.interrupted()) {
                socket.receive(serverDatagram);  //attendo il prossimo pacchetto da server
                //converto in string il messaggio contenuto nel buffer
                received = new String(serverDatagram.getData(), 0, serverDatagram.getLength(), "ISO-8859-1");
                areaChat.append(received + "\n");
                //Stringa presa da stackoverflow.com per scrollare automaticamente il JScrollPane nella posizione più favorevole per l'utente
                areaChat.setCaretPosition(areaChat.getDocument().getLength());
            }
            socket.close();

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(chatGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(chatGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws UnknownHostException, SocketException {

        address = InetAddress.getByName(IP_address);
        UDP_port = 1077;

        socket = new DatagramSocket();

        //Avvio Interfaccia
        Runnable r = new Runnable() {
            public void run() {
                try {
                    new chatGUI().setVisible(true);
                } catch (InterruptedException ex) {
                    Logger.getLogger(chatGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        EventQueue.invokeLater(r);
    }

    //Non utilizzato
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void run() {

    }

}
