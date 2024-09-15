package com.amazon.ata.executorservice.futures;

import com.amazon.ata.executorservice.coralgenerated.customer.GetCustomerDevicesRequest;
import com.amazon.ata.executorservice.customer.CustomerService;
import com.amazon.ata.executorservice.devicecommunication.RingDeviceCommunicatorService;
import com.amazon.ata.executorservice.util.KnownRingDeviceFirmwareVersions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Phase2Test {
    private static final int NUM_ITERATIONS = 5;
    private static final String TRIAL_CUSTOMER_ID = "1";

    private CustomerService customerService;
    private RingDeviceCommunicatorService deviceCommunicatorService;
    private ComplianceEnforcer complianceEnforcer;
    private ReferenceEnforcer referenceEnforcer;
    private List<String> deviceIds;

    @BeforeEach
    private void setup() {
        customerService = CustomerService.getClient();
        deviceCommunicatorService = RingDeviceCommunicatorService.getClient();
        complianceEnforcer = new ComplianceEnforcer(customerService, deviceCommunicatorService);
        referenceEnforcer = new ReferenceEnforcer(customerService, deviceCommunicatorService);
        deviceIds = customerService.getCustomerDevices(GetCustomerDevicesRequest.builder()
                .withCustomerId(TRIAL_CUSTOMER_ID)
                .build()).getDeviceIds();
    }

    @Test
    public void findUpdatesForCustomer_afterRefactoring_findsSameDevices() throws Exception {
        // GIVEN
        // A known list of non-compliant devices for a customer
        List<String> refIds = referenceEnforcer.findUpdatesForCustomer(TRIAL_CUSTOMER_ID,
                KnownRingDeviceFirmwareVersions.PINKY);

        // WHEN
        // We attempt to determine what devices a customer needs to update
        List<String> noncompliantIds = complianceEnforcer.findUpdatesForCustomer(TRIAL_CUSTOMER_ID,
                KnownRingDeviceFirmwareVersions.PINKY);

        // THEN
        // The device list contains the known devices
        assertTrue(noncompliantIds.containsAll(refIds) && refIds.size() == noncompliantIds.size(),
                String.format("Expected known devices %s to need update: got %s!", refIds, noncompliantIds));

    }

    @Test
    void findUpdatesForCustomer_afterRefactoring_hasImprovedPerformance() throws Exception {
        // GIVEN
        // A known list of non-compliant devices for a customer
        // A reference time to find the devices
        double referenceAvg = runProfile(() ->
                referenceEnforcer.findUpdatesForCustomer(TRIAL_CUSTOMER_ID, KnownRingDeviceFirmwareVersions.PINKY));

        // WHEN
        // We update the devices with the improved algorithm
        double improvedAvg = runProfile(() ->
                complianceEnforcer.findUpdatesForCustomer(TRIAL_CUSTOMER_ID, KnownRingDeviceFirmwareVersions.PINKY));

        // THEN
        // The average run time has improved
        double benchmark = referenceAvg * 0.9;
        assertTrue(improvedAvg < benchmark,
                String.format("Expected findUpdatesForCustomer to improve reference implementation average runtime "
                        + "[%02.02f] by at least 10%%, to [%02.02f]. Measured average runtime was [%02.02f]!",
                        referenceAvg, benchmark, improvedAvg));
    }

    private double runProfile(Runnable trial) {
        long startTime = System.nanoTime();
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            trial.run();
        }
        long endTime = System.nanoTime();
        double avgTime = (double)(endTime - startTime) / 1000000000.0 / (double)NUM_ITERATIONS;
        return avgTime;
    }
}
