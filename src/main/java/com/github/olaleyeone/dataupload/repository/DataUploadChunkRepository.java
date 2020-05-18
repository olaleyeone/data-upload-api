package com.github.olaleyeone.dataupload.repository;

import com.github.olaleyeone.dataupload.data.dto.DataChunk;
import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import com.github.olaleyeone.dataupload.data.entity.DataUploadChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DataUploadChunkRepository extends JpaRepository<DataUploadChunk, Long> {

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

    @Query("SELECT c FROM DataUploadChunk c WHERE c.dataUpload=?1 ORDER BY c.start")
    List<DataUploadChunk> getAllData(DataUpload dataUpload);

    @Query("SELECT c.id FROM DataUploadChunk c WHERE c.dataUpload=?1 ORDER BY c.start")
    List<Long> getChunkIds(DataUpload dataUpload);
}
