package com.xboard.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.google.gson.Gson;

import java.util.HashMap;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

public class GreetingLambda implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayProxyResponseEvent>
{

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayV2WebSocketEvent input, Context context) {
        DynamoDbClient client = DynamoDbClient.create();
        Gson s = new Gson();
//        JsonObject object = JsonParser.parseString(s.toJson(input)).getAsJsonObject();
        String connectionId = input.getRequestContext().getConnectionId();

        HashMap<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put("connectionId", AttributeValue.builder().s(connectionId).build());
        itemValues.put("table",AttributeValue.builder().s("simplechat_connections").build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(System.getenv("TABLE_NAME"))
                .item(itemValues)
                .build();

        try{
            client.putItem(request);
            System.out.println(System.getenv("TABLE_NAME") + " was successfully updated");

        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", System.getenv("TABLE_NAME"));
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
            System.exit(1);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }



        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        response.setBody( "Connected.");
        return response;
    }
}
