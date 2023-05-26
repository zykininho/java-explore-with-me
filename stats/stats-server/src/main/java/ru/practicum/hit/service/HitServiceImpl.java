package ru.practicum.hit.service;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;
    private final HitMapper hitMapper;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

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
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("start", startDate);
        parameters.addValue("end", endDate);
        parameters.addValue("uris", uris);
        String sql = "SELECT app, uri, COUNT(DISTINCT ip) AS hits\n" +
                "FROM public.hits\n" +
                "WHERE created_date >= :start AND created_date <= :end AND uri IN (:uris)\n" +
                "GROUP BY hits.app, hits.uri\n" +
                "ORDER BY COUNT(DISTINCT ip) DESC";
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> createViewStats(rs));
    }

    private List<ViewStatsDto> getUniqueStatsFromStartToEnd(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Extracting unique stats from start={} to end={}", startDate, endDate);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("start", startDate);
        parameters.addValue("end", endDate);
        String sql = "SELECT app, uri, COUNT(DISTINCT ip) AS hits\n" +
                "FROM public.hits\n" +
                "WHERE created_date >= :start AND created_date <= :end\n" +
                "GROUP BY hits.app, hits.uri\n" +
                "ORDER BY COUNT(DISTINCT ip) DESC";
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> createViewStats(rs));
    }

    private List<ViewStatsDto> getStatsFromStartToEndWithUris(LocalDateTime startDate, LocalDateTime endDate, List<String> uris) {
        log.info("Extracting all stats from start={} to end={} and uri in ({})", startDate, endDate, uris);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("start", startDate);
        parameters.addValue("end", endDate);
        parameters.addValue("uris", uris);
        String sql = "SELECT app, uri, COUNT(ip) AS hits\n" +
                "FROM public.hits\n" +
                "WHERE created_date >= :start AND created_date <= :end AND uri IN (:uris)\n" +
                "GROUP BY hits.app, hits.uri\n" +
                "ORDER BY COUNT(ip) DESC";
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> createViewStats(rs));
    }

    private List<ViewStatsDto> getStatsFromStartToEnd(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Extracting all stats from start={} to end={}", startDate, endDate);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("start", startDate);
        parameters.addValue("end", endDate);
        String sql = "SELECT app, uri, COUNT(ip) AS hits\n" +
                "FROM public.hits\n" +
                "WHERE created_date >= :start AND created_date <= :end\n" +
                "GROUP BY hits.app, hits.uri\n" +
                "ORDER BY COUNT(ip) DESC";
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> createViewStats(rs));
    }

    private ViewStatsDto createViewStats(ResultSet rs) throws SQLException {
        return ViewStatsDto.builder()
                .app(rs.getString("app"))
                .uri(rs.getString("uri"))
                .hits(rs.getInt("hits"))
                .build();
    }

}