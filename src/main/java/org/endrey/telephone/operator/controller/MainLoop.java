package org.endrey.telephone.operator.controller;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.endrey.telephone.operator.entity.UDR;
import org.endrey.telephone.operator.service.CDRService;
import org.endrey.telephone.operator.service.UDRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for the main loop of the Telephone Operator application.
 * It provides a command-line interface for the user to interact with the application.
 */
@Component
public class MainLoop {

    @Autowired
    private CDRService cdrService;

    @Autowired
    private UDRService udrService;

    private final Scanner scanner = new Scanner(System.in);
    
    public void run() {
        printListUDR(udrService.generateReport());
        while (true) {
            System.out.println("1. Generate CDR");
            System.out.println("2. Generate Usage Data Report");
            System.out.println("3. Exit");
            System.out.print("Select an action: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    generateCDR();
                    break;
                case 2:
                    generateUDR();
                    break;
                case 3:
                    System.exit(0);
                default:
                    System.out.println("Incorrect selection. Try again.");
            }
        }
    }

    private void generateCDR() {
        while (true) {
            System.out.println("Generate CDR");
            System.out.println("1. Generate CDR for month");
            System.out.println("2. Generate CDR for period");
            System.out.println("3. Back");
            System.out.print("Select an action: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Enter year: ");
                    System.out.println("Enter month: ");
                    int month = scanner.nextInt();
                    scanner.nextLine();
                    cdrService.generateCDRByMonth(month);
                    break;
                case 2:
                    cdrService.generateCDRForPeriod();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Incorrect selection. Try again.");
            }
        }
    }

    private void generateUDR() {
        while (true) {
            System.out.println("Generate UDR");
            System.out.println("1. Generate Report");
            System.out.println("2. Generate Report by msisdn");
            System.out.println("3. Generate Report by msisdn and month");
            System.out.println("4. Back");
            System.out.print("Select an action: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            String msisdn;
            switch (choice) {
                case 1:
                    printListUDR(udrService.generateReport());
                    break;
                case 2:
                    System.out.println("Enter msisdn: ");
                    msisdn = scanner.nextLine();
                    Map<Integer, UDR> udrMap = udrService.generateReportByPhoneNumber(msisdn);
                    if (udrMap.isEmpty()) {
                        System.out.println("No data for this msisdn.");
                        break;
                    }
                    for (int month : udrMap.keySet()) {
                        System.out.println("Month: " + month);
                        printUDR(udrMap.get(month));
                    }
                    break;
                case 3:
                    System.out.println("Enter msisdn: ");
                    msisdn = scanner.nextLine();
                    System.out.println("Enter month: ");
                    int month = scanner.nextInt();
                    scanner.nextLine();
                    printListUDR(udrService.generateReportByPhoneNumberAndMonth(msisdn, month));
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Incorrect selection. Try again.");
            }
        }
    }

    private void printListUDR(List<UDR> udrList) {
        if (udrList.isEmpty()) {
            System.out.println("No data.");
            return;
        }
        for (UDR udr : udrList) {
            printUDR(udr);
        }
    }

    private void printUDR(UDR udr) {
        System.out.println("UDR for MSISDN: " + udr.getMsisdn());
        System.out.println("Incoming Call: " + udr.getIncomingCall().getTotalTime());
        System.out.println("Outcoming Call: " + udr.getOutcomingCall().getTotalTime());
    }
}
