package cn;

import com.google.cloud.compute.v1.*;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListVMIP implements HttpFunction {
    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        String projectId = ConfigManager.getProjectId();
        String zone = ConfigManager.getZone();
        String instanceGroup = ConfigManager.getGrpcServersGroupName();

        List<String> ips = new ArrayList<>();

        try (
                InstanceGroupsClient groupClient = InstanceGroupsClient.create();
                InstancesClient instancesClient = InstancesClient.create()
        ) {
            ListInstancesInstanceGroupsRequest groupRequest =
                    ListInstancesInstanceGroupsRequest.newBuilder()
                            .setProject(projectId)
                            .setZone(zone)
                            .setInstanceGroup(instanceGroup)
                            .setInstanceGroupsListInstancesRequestResource(InstanceGroupsListInstancesRequest.newBuilder().build())
                            .build();

            for (InstanceWithNamedPorts instanceRef : groupClient.listInstances(groupRequest).iterateAll()) {
                String instanceUrl = instanceRef.getInstance();
                String instanceName = instanceUrl.substring(instanceUrl.lastIndexOf("/") + 1);

                Instance instance =
                        instancesClient.get(projectId, zone, instanceName);

                if(instance.getStatus().equals("RUNNING")) {
                    if (
                            instance.getNetworkInterfacesCount() > 0 &&
                                    instance.getNetworkInterfaces(0).getAccessConfigsCount() > 0
                    ) {
                        String ip = instance
                                .getNetworkInterfaces(0)
                                .getAccessConfigs(0)
                                .getNatIP();

                        ips.add(ip);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            httpResponse.setStatusCode(500);
            httpResponse.getWriter().write("Error retrieving VM IPs: " + e.getMessage());
            return;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("instanceGroup", instanceGroup);
        result.put("zone", zone);
        result.put("ips", ips);

        httpResponse.appendHeader("Content-Type", "application/json");

        BufferedWriter writer = httpResponse.getWriter();
        writer.write(new Gson().toJson(result));
    }
}
