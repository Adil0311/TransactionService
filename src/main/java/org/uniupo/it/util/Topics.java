package org.uniupo.it.util;

public class Topics {
    private final static String BASE_TOPIC = "istituto/%s/macchina/%s";
    public static final String DISPLAY_TOPIC_UPDATE = BASE_TOPIC+"/frontend/screen/update";
    public static final String DISPENSER_TOPIC = BASE_TOPIC+"/dispenser";
    public static final String DISPENSER_TOPIC_DISPENSE = BASE_TOPIC+"/dispenser/dispense";
    public static final String ASSISTANCE_CHECK_MACHINE_STATUS_TOPIC = BASE_TOPIC+"/assistance/checkMachineStatus";
    public static final String BALANCE_CHECK_TOPIC = BASE_TOPIC+"/balance/checkBalance";
    public static final String BALANCE_CHECK_TOPIC_RESPONSE = BASE_TOPIC+"/transaction/checkBalanceResponse";
    public static final String DISPENSE_COMPLETED_TOPIC = BASE_TOPIC+"/dispenser/dispenseCompleted";
    public static final String TRANSACTION_NEW_COIN_INSERTED_TOPIC = BASE_TOPIC+"/transaction/newCoinInserted";
    public static final String TRANSACTION_CANCEL_SELECTION_TOPIC = BASE_TOPIC+"/transaction/cancelSelection";
    public static final String TRANSACTION_NEW_SELECTION_TOPIC = BASE_TOPIC+"/transaction/newSelection";
    public static final String TRANSACTION_CONSUMABLE_AVAILABILITY_TOPIC_RESPONSE = BASE_TOPIC+"/transaction/consumableAvailabilityResponse";
    public static final String RESPONSE_ASSISTANCE_CHECK_MACHINE_STATUS_TOPIC = BASE_TOPIC+"/transaction/checkMachineStatusResponse";

    public static final String BALANCE_RETURN_MONEY_TOPIC = BASE_TOPIC+"/balance/returnMoney";

    public static final String REGISTER_TRANSACTION_TOPIC= "management/transaction/register";

    public static final String KILL_SERVICE_TOPIC = "macchinette/%s/%s/killService";

}
