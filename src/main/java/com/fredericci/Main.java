package com.fredericci;

import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.studio.jetty.LangGraphStreamingServerJetty;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;

public class Main {
    public static void main(String[] args) throws Exception {
        
        var workflow = new StateGraph<>(AgentState::new)
                .addEdge(START, "A")
                .addNode("A", node_async(new AgentStateAsyncNodeAction()))
                .addNode("B", node_async(new AgentStateAsyncNodeAction()))
                .addNode("C", node_async(new AgentStateAsyncNodeAction()))
                .addNode("D", node_async(new AgentStateAsyncNodeAction()))
                .addConditionalEdges(
                        "A",
                        edge_async(state -> {
                            Map<String, Object> data = state.data();
                            String input = (String) data.get("input");
                            
                            if (input != null && input.equals("D")) {
                                return "Goto D";
                            }
                            
                            if (input != null && input.equals("C")) {
                                return "Goto C";
                            }
                            
                            return "Goto B";
                        }),
                        mapOf("Goto B", "B", "Goto C", "C", "Goto D","D")
                )
                .addEdge("B", END)
                .addEdge("D", END)
                .addEdge("C", END)
                ;
        
        var saver = new MemorySaver();
        
        var server = LangGraphStreamingServerJetty.builder()
                .port(8080)
                .title("LANGGRAPH4j - TEST")
                .stateGraph(workflow)
                .addInputStringArg("input")
                .checkpointSaver(saver)
                .build();
        
        server.start().join();
        
        
    }
    
    private static class AgentStateAsyncNodeAction implements NodeAction<AgentState> {
        
        @Override
        public Map<String, Object> apply(AgentState agentState) {
            return Map.of();
        }
    }
}