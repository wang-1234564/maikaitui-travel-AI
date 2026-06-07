package com.maikaitui.ai.repository;

import com.maikaitui.ai.document.ChatSessionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 聊天会话 Repository
 */
@Repository
public interface ChatSessionRepository extends MongoRepository<ChatSessionDocument, String> {

    /** 查询用户会话列表 — 排除 messages 字段避免传输全部消息 */
    @Query(value = "{ 'userId': ?0 }", fields = "{ 'messages': 0 }", sort = "{ 'updateTime': -1 }")
    List<ChatSessionDocument> findByUserIdOrderByUpdateTimeDesc(Long userId);

    /** 删除用户所有会话 */
    void deleteByUserId(Long userId);
}
