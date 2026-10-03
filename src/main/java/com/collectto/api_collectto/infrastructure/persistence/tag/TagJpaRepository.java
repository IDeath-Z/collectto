package com.collectto.api_collectto.infrastructure.persistence.tag;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagJpaRepository extends JpaRepository<TagJpaEntity, UUID> {

    @Query(value = """
        SELECT * FROM tags
        WHERE name ILIKE :prefix || '%'
        ORDER BY name
        LIMIT :limit
        """, nativeQuery = true
    )
    List<TagJpaEntity> findSuggestions(@Param("prefix") String prefix, @Param("limit") int limit);

    @Query(value = """
        SELECT DISTINCT t.* FROM tags t
        JOIN collection_tags ct ON t.tag_id = ct.tag_id
        JOIN collection_follows ufc ON ct.collection_id = ufc.collection_id
        WHERE ufc.follower_id = :userId
        LIMIT 20
        """, nativeQuery = true)
    List<TagJpaEntity> findFavoriteTagsByUserId(@Param("userId") UUID userId);

    Optional<TagJpaEntity> findByName(String name);

    Page<TagJpaEntity> findByNameContainingIgnoreCaseOrderByUsageCountDesc(String name, Pageable pageable);

    // [Grafo de recomendação] Busca as tags de UM usuário e quantas vezes ele usa
    // cada uma, somando 3 origens: itens que ele criou, coleções que ele criou e
    // coleções que ele segue. Usado tanto para calcular o gosto do usuário-alvo
    // quanto o de cada "vizinho" encontrado no grafo.
    @Query(value = """
        SELECT tag_id AS tagId, COUNT(*) AS weight FROM (
            SELECT it.tag_id FROM item_tags it
            JOIN items i ON i.item_id = it.item_id
            WHERE i.user_id = :userId AND i.is_active = true

            UNION ALL

            SELECT ct.tag_id FROM collection_tags ct
            JOIN collections c ON c.collection_id = ct.collection_id
            WHERE c.user_id = :userId AND c.is_active = true

            UNION ALL

            SELECT ct.tag_id FROM collection_tags ct
            JOIN collection_follows cf ON cf.collection_id = ct.collection_id
            WHERE cf.follower_id = :userId
        ) AS combined
        GROUP BY tag_id
        """, nativeQuery = true)
    List<TagWeightProjection> findTagWeightsByUserId(@Param("userId") UUID userId);

    // [Grafo de recomendação] Dado um conjunto de tags, encontra outros usuários
    // (excluindo o próprio) que usam pelo menos uma delas. Esses usuários viram
    // os "vizinhos" que o algoritmo BFS vai visitar em seguida.
    @Query(value = """
        SELECT DISTINCT user_id FROM (
            SELECT i.user_id FROM item_tags it
            JOIN items i ON i.item_id = it.item_id
            WHERE it.tag_id IN (:tagIds) AND i.is_active = true

            UNION

            SELECT c.user_id FROM collection_tags ct
            JOIN collections c ON c.collection_id = ct.collection_id
            WHERE ct.tag_id IN (:tagIds) AND c.is_active = true

            UNION

            SELECT cf.follower_id FROM collection_tags ct
            JOIN collection_follows cf ON cf.collection_id = ct.collection_id
            WHERE ct.tag_id IN (:tagIds)
        ) AS neighbors
        WHERE user_id != :excludeUserId
        """, nativeQuery = true)
    List<UUID> findUserIdsByTagIds(@Param("tagIds") Set<UUID> tagIds, @Param("excludeUserId") UUID excludeUserId);
}