package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        int trainId = bookTicketEntryDto.getTrainId();
        Train train = trainRepository.findById(trainId).get();
        int noOfseat = train.getNoOfSeats();
        int  noOfbBookedTickets = train.getBookedTickets().size();
        if((noOfseat-noOfbBookedTickets) > bookTicketEntryDto.getNoOfSeats()){
             throw new Exception("Less tickets are available");
        }
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");

        String fromStation = bookTicketEntryDto.getFromStation().toString();
        String toStation = bookTicketEntryDto.getToStation().toString();

        boolean fsta = false; boolean tsta = false;
        String troute = train.getRoute(); int unitDistanceCount = 0;
        String[] stations = troute.split(",");
        for(int i = 0 ; i < stations.length; i++){
            if(stations[i].equals(fromStation)){
                fsta = true;
                unitDistanceCount = i;
            }
            if(stations[i].equals(toStation)){
                tsta = true;
                unitDistanceCount -= i;
            }
        }
        if(!fsta || !tsta){
            throw new Exception("Invalid stations");
        }

        Ticket ticket = new Ticket();
        List<Passenger> passengerList = new ArrayList<>();
        List<Integer> passengerIdList = bookTicketEntryDto.getPassengerIds();
        for(int i = 0  ; i < passengerIdList.size() ; i++){
            Passenger pass = passengerRepository.findById(passengerIdList.get(i)).get();
            passengerList.add(pass);
        }
        ticket.setPassengersList(passengerList);
        ticket.setTrain(train);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(bookTicketEntryDto.getNoOfSeats()*300*unitDistanceCount);

        //Save the bookedTickets in the train Object
        train.getBookedTickets().add(ticket);
        train.setNoOfSeats(train.getNoOfSeats() - bookTicketEntryDto.getNoOfSeats());
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger.getBookedTickets().add(ticket);
       //And the end return the ticketId that has come from db

       return ticketRepository.save(ticket).getTicketId();

    }
}
