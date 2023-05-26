package ru.practicum.hit.service;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.hit.dto.EndpointHitDto;
import ru.practicum.hit.mapper.HitMapper;
import ru.practicum.hit.model.EndpointHit;
import ru.practicum.hit.repo.HitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.ViewStatsDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;
    private final HitMapper hitMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public EndpointHitDto addNewHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = hitMapper.toEndpointHit(endpointHitDto);
        EndpointHit savedEndpointHit = hitRepository.save(endpointHit);
        log.info("Добавлен новый просмотр {}", endpointHit);
        return hitMapper.toEndpointHitDto(savedEndpointHit);
    }

    @Override
    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, String unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDate = LocalDateTime.parse(start, formatter);
        LocalDateTime endDate = LocalDateTime.parse(end, formatter);
        boolean onlyUnique = Boolean.parseBoolean(unique);
        List<ViewStatsDto> viewStats;
        if (onlyUnique) {
            if (uris != null) {
                viewStats = getUniqueStatsFromStartToEndWithUris(startDate, endDate, uris);
            } else {
                viewStats = getUniqueStatsFromStartToEnd(startDate, endDate);
            }
        } else {
            if (uris != null) {
                viewStats = getStatsFromStartToEndWithUris(startDate, endDate, uris);
            } else {
                viewStats = getStatsFromStartToEnd(startDate, endDate);
            }
        }
        return viewStats;
    }

    private List<ViewStatsDto> getUniqueStatsFromStartToEndWithUris(LocalDateTime startDate, LocalDateTime endDate, List<String> uris) {
        log.info("Extracting unique stats from start={} to end={} and uri in ({})", startDate, endDate, uris);
        String sql = "SELECT app, uri, COUNT(DISTINCT ip) AS hits\n" +
                "FROM public.hits\n" +
                "WHERE created_date >= ? AND created_date <= ?\tAND uri IN (?)\n" +
                "GROUP BY hits.app, hits.uri\n" +
                "ORDER BY COUNT(DISTINCT ip) DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> createViewStats(rs), startDate, endDate, uris);
    }

    private List<ViewStatsDto> getUniqueStatsFromStartToEnd(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Extracting unique stats from start={} to end={}", startDate, endDate);
        String sql = "SELECT app, uri, COUNT(DISTINCT ip) AS hits\n" +
                "FROM public.hits\n" +
                "WHERE created_date >= ? AND created_date <= ?\n" +
                "GROUP BY hits.app, hits.uri\n" +
                "ORDER BY COUNT(DISTINCT ip) DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> createViewStats(rs), startDate, endDate);
    }

    private List<ViewStatsDto> getStatsFromStartToEndWithUris(LocalDateTime startDate, LocalDateTime endDate, List<String> uris) {
        log.info("Extracting all stats from start={} to end={} and uri in ({})", startDate, endDate, uris);
        String sql = "SELECT app, uri, COUNT(ip) AS hits\n" +
                "FROM public.hits\n" +
                "WHERE created_date >= ? AND created_date <= ?\tAND uri IN (?)\n" +
                "GROUP BY hits.app, hits.uri\n" +
                "ORDER BY COUNT(ip) DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> createViewStats(rs), startDate, endDate, uris);
    }

    private List<ViewStatsDto> getStatsFromStartToEnd(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Extracting all stats from start={} to end={}", startDate, endDate);
        String sql = "SELECT app, uri, COUNT(ip) AS hits\n" +
                "FROM public.hits\n" +
                "WHERE created_date >= ? AND created_date <= ?\n" +
                "GROUP BY hits.app, hits.uri\n" +
                "ORDER BY COUNT(ip) DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> createViewStats(rs), startDate, endDate);
    }

    private ViewStatsDto createViewStats(ResultSet rs) throws SQLException {
        return ViewStatsDto.builder()
                .app(rs.getString("app"))
                .uri(rs.getString("uri"))
                .hits(rs.getInt("hits"))
                .build();
    }

}