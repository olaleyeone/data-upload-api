package com.github.olaleyeone.dataupload.repository;

import com.github.olaleyeone.dataupload.data.dto.DataChunk;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DataUploadChunkRepository extends JpaRepository<DataUploadChunk, Long> {

    @Transactional
    @Override
    Optional<DataUploadChunk> findById(Long aLong);

    @Query("SELECT COUNT(c) FROM DataUploadChunk c WHERE c.dataUpload=?1 AND NOT (?2>=(c.start+c.size) OR (?2+?3)<=c.start)")
    int countByRange(DataUpload dataUpload, Long start, Integer size);

    @Query("SELECT SUM(c.size) FROM DataUploadChunk c WHERE c.dataUpload=?1")
    long sumData(DataUpload dataUpload);

    @Query("SELECT new com.github.olaleyeone.dataupload.data.dto.DataChunk(c.start, c.size) FROM DataUploadChunk c" +
            " WHERE c.dataUpload=?1" +
            " AND c.id NOT IN " +
            " (SELECT c.id FROM" +
            " DataUploadChunk c," +
            " DataUploadChunk before," +
            " DataUploadChunk after" +
            " WHERE c.dataUpload=?1 AND before.dataUpload=?1 AND after.dataUpload=?1" +
            " AND (before.start+before.size)=c.start" +
            " AND (c.start+c.size)=after.start" +
            ") ORDER BY c.start")
    List<DataChunk> getOpenChunks(DataUpload dataUpload);

    @Query("SELECT new com.github.olaleyeone.dataupload.data.dto.DataChunk(c.id, c.start, c.size)" +
            " FROM DataUploadChunk c" +
            " WHERE c.dataUpload=?1" +
            " AND (" +
            " (c.start+c.size-1 >= ?2)" +
            " OR" +
            " (c.start-1 <= ?3)" +
            ")" +
            " ORDER BY c.start")
    List<DataChunk> getChunks(DataUpload dataUpload, long start, long end);

    @Query("SELECT new com.github.olaleyeone.dataupload.data.dto.DataChunk(c.id, c.start, c.size)" +
            " FROM DataUploadChunk c" +
            " WHERE c.dataUpload=?1" +
            " ORDER BY c.start")
    List<DataChunk> getChunks(DataUpload dataUpload);

    @Query("SELECT c.id FROM DataUploadChunk c WHERE c.dataUpload=?1 ORDER BY c.start")
    List<Long> getChunkIds(DataUpload dataUpload);

    @Query("SELECT MAX(c.createdOn) FROM DataUploadChunk c WHERE c.dataUpload=?1")
    LocalDateTime findLatestUploadTime(DataUpload dataUpload);

    @Query(nativeQuery = true, value = "SELECT substr(data, ?2, ?3) FROM data_upload_chunk WHERE id = ?1")
    byte[] getData(Long chunkId, int offset, int length);
}
