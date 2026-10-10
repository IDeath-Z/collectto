package com.collectto.api_collectto.domain.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Calcula quais tags recomendar para um usuário, com base nas tags
 * de "vizinhos" (outros usuários que compartilham pelo menos uma tag com ele).
 *
 * Esta classe representa o "andar pelo grafo com peso": ela nunca acessa
 * banco de dados nem sabe o que é UUID de usuário — só recebe números prontos
 * e devolve a pontuação de cada tag candidata.
 */
public final class TagAffinityRecommender {

    // Construtor privado: essa classe só tem métodos estáticos, nunca é "instanciada"
    private TagAffinityRecommender() {}

    /**
     * @param ownTagWeights       as tags do próprio usuário e o quanto ele usa cada uma
     *                            
     * @param neighborTagWeights  as tags de UM vizinho e o quanto ELE usa cada uma
     *                            
     * @param scoreAccumulator    o "placar" que vai sendo somado a cada vizinho visitado
     *                        
     */
    public static void accumulateNeighborScore(
            Map<UUID, Integer> ownTagWeights,
            Map<UUID, Integer> neighborTagWeights,
            Map<UUID, Integer> scoreAccumulator) {

        for (Map.Entry<UUID, Integer> entry : neighborTagWeights.entrySet()) {
            UUID tagId = entry.getKey();
            int neighborWeight = entry.getValue();

            // Regra principal: só queremos tags NOVAS, que o usuário ainda não usa
            // Pula o que o usuário já tem, para não recomendar o óbvio
            if (ownTagWeights.containsKey(tagId)) {
                continue;
            }

            // Soma o peso desse vizinho ao placar da tag.
            // Se dois vizinhos diferentes tiverem a mesma tag, ela vai somando o peso de cada um.
            scoreAccumulator.merge(tagId, neighborWeight, Integer::sum);
        }
    }

    /**
     * Monta o placar do zero e já devolve pronto, sem precisar criar o Map na mão
     * antes de chamar o método acima.
     */
    public static Map<UUID, Integer> newEmptyScoreboard() {
        return new HashMap<>();
    }
}