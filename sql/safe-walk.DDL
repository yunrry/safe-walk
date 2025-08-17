-- safe_walk.accident_statics definition
USE `safe_walk`;

CREATE TABLE `accident_statics` (
                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                    `accident_year` int NOT NULL COMMENT '연도',
                                    `accident_type_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '대상사고 구분명',
                                    `sido_code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '법정동코드',
                                    `sido_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '시도명',
                                    `accident_count` int DEFAULT NULL COMMENT '사고건수',
                                    `accident_count_ratio` decimal(5,2) DEFAULT NULL COMMENT '사고건수 구성비',
                                    `death_count` int DEFAULT NULL COMMENT '사망자수',
                                    `death_count_ratio` decimal(5,2) DEFAULT NULL COMMENT '사망자수 구성비',
                                    `fatality_rate` decimal(5,2) DEFAULT NULL COMMENT '치사율',
                                    `injury_count` int DEFAULT NULL COMMENT '부상자수',
                                    `injury_count_ratio` decimal(5,2) DEFAULT NULL COMMENT '부상자수 구성비',
                                    `accident_per_100k_population` decimal(10,2) DEFAULT NULL COMMENT '인구10만명당 사고건수',
                                    `accident_per_10k_vehicles` decimal(10,2) DEFAULT NULL COMMENT '자동차1만대당 사고건수',
                                    `speeding_count` int DEFAULT NULL COMMENT '과속',
                                    `center_line_violation_count` int DEFAULT NULL COMMENT '중앙선 침범',
                                    `signal_violation_count` int DEFAULT NULL COMMENT '신호위반',
                                    `unsafe_distance_count` int DEFAULT NULL COMMENT '안전거리 미확보',
                                    `unsafe_driving_count` int DEFAULT NULL COMMENT '안전운전 의무 불이행',
                                    `pedestrian_protection_violation_count` int DEFAULT NULL COMMENT '보행자 보호의무 위반',
                                    `other_violation_count` int DEFAULT NULL COMMENT '기타',
                                    `vehicle_pedestrian_accident_count` int DEFAULT NULL COMMENT '차대사람',
                                    `vehicle_vehicle_accident_count` int DEFAULT NULL COMMENT '차대차',
                                    `single_vehicle_accident_count` int DEFAULT NULL COMMENT '차량단독',
                                    `railroad_crossing_accident_count` int DEFAULT NULL COMMENT '철길건널목',
                                    `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                    `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`),
                                    KEY `idx_year` (`accident_year`),
                                    KEY `idx_sido` (`sido_code`),
                                    KEY `idx_accident_type` (`accident_type_name`)
) ENGINE=InnoDB AUTO_INCREMENT=43771 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='교통사고 통계 데이터';



-- safe_walk.administrative_legal_dongs definition

CREATE TABLE `administrative_legal_dongs` (
                                              `id` bigint NOT NULL AUTO_INCREMENT,
                                              `code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'code',
                                              `sido` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'sido',
                                              `sigungu` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'sigungu',
                                              `eup_myeon_dong` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '읍면동명',
                                              `sub_level` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '하위 행정구역',
                                              `latitude` decimal(12,9) DEFAULT NULL COMMENT '위도',
                                              `longitude` decimal(12,9) DEFAULT NULL COMMENT '경도',
                                              `code_type` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '코드 종류',
                                              `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                              `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                              PRIMARY KEY (`id`),
                                              UNIQUE KEY `uk_code` (`code`),
                                              KEY `idx_code` (`code`),
                                              KEY `idx_sido` (`sido`),
                                              KEY `idx_sigungu` (`sigungu`),
                                              KEY `idx_eup_myeon_dong` (`eup_myeon_dong`),
                                              KEY `idx_location` (`longitude`,`latitude`),
                                              KEY `idx_code_type` (`code_type`)
) ENGINE=InnoDB AUTO_INCREMENT=34220 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='행정 법정동 중심좌표 데이터';

-- safe_walk.elderly_pedestrian_accident_hotspots definition

CREATE TABLE `elderly_pedestrian_accident_hotspots` (
                                                        `id` bigint NOT NULL AUTO_INCREMENT,
                                                        `accident_hotspot_fid` bigint NOT NULL COMMENT '사고다발지fid',
                                                        `accident_hotspot_id` bigint NOT NULL COMMENT '사고다발지id',
                                                        `sido_code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '법정동코드',
                                                        `point_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '지점코드',
                                                        `sido_sigungu_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '시도시군구명',
                                                        `point_name` text COLLATE utf8mb4_unicode_ci COMMENT '지점명',
                                                        `legal_dong` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '법정동',
                                                        `accident_count` int DEFAULT NULL COMMENT '사고건수',
                                                        `casualty_count` int DEFAULT NULL COMMENT '사상자수',
                                                        `death_count` int DEFAULT NULL COMMENT '사망자수',
                                                        `serious_injury_count` int DEFAULT NULL COMMENT '중상자수',
                                                        `minor_injury_count` int DEFAULT NULL COMMENT '경상자수',
                                                        `injury_report_count` int DEFAULT NULL COMMENT '부상신고자수',
                                                        `longitude` decimal(12,9) DEFAULT NULL COMMENT '경도',
                                                        `latitude` decimal(12,9) DEFAULT NULL COMMENT '위도',
                                                        `hotspot_polygon` json DEFAULT NULL COMMENT '다발지역폴리곤',
                                                        `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                                        `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                        PRIMARY KEY (`id`),
                                                        UNIQUE KEY `uk_hotspot_fid` (`accident_hotspot_fid`),
                                                        KEY `idx_hotspot_fid` (`accident_hotspot_fid`),
                                                        KEY `idx_hotspot_id` (`accident_hotspot_id`),
                                                        KEY `idx_sido_code` (`sido_code`),
                                                        KEY `idx_point_code` (`point_code`),
                                                        KEY `idx_location` (`longitude`,`latitude`),
                                                        KEY `idx_legal_dong` (`legal_dong`)
) ENGINE=InnoDB AUTO_INCREMENT=11459 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='노인 보행자 사고 다발지 데이터';


-- safe_walk.emd_data definition

CREATE TABLE `emd_data` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `EMD_CD` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '읍면동코드',
                            `EMD_ENG_NM` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '읍면동영문명',
                            `EMD_KOR_NM` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '읍면동한글명',
                            `Polygon` json NOT NULL COMMENT '폴리곤좌표',
                            `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_emd_cd` (`EMD_CD`),
                            KEY `idx_emd_cd` (`EMD_CD`),
                            KEY `idx_emd_kor_nm` (`EMD_KOR_NM`),
                            KEY `idx_emd_eng_nm` (`EMD_ENG_NM`)
) ENGINE=InnoDB AUTO_INCREMENT=5066 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='읍면동 경계 데이터';


-- safe_walk.holiday_accident_hotspots definition

CREATE TABLE `holiday_accident_hotspots` (
                                             `id` bigint NOT NULL AUTO_INCREMENT,
                                             `accident_hotspot_fid` bigint NOT NULL COMMENT '사고다발지fid',
                                             `accident_hotspot_id` bigint NOT NULL COMMENT '사고다발지id',
                                             `sido_code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '법정동코드',
                                             `point_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '지점코드',
                                             `sido_sigungu_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '시도시군구명',
                                             `point_name` text COLLATE utf8mb4_unicode_ci COMMENT '지점명',
                                             `legal_dong` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '법정동',
                                             `accident_count` int DEFAULT NULL COMMENT '사고건수',
                                             `casualty_count` int DEFAULT NULL COMMENT '사상자수',
                                             `death_count` int DEFAULT NULL COMMENT '사망자수',
                                             `serious_injury_count` int DEFAULT NULL COMMENT '중상자수',
                                             `minor_injury_count` int DEFAULT NULL COMMENT '경상자수',
                                             `injury_report_count` int DEFAULT NULL COMMENT '부상신고자수',
                                             `longitude` decimal(12,9) DEFAULT NULL COMMENT '경도',
                                             `latitude` decimal(12,9) DEFAULT NULL COMMENT '위도',
                                             `hotspot_polygon` json DEFAULT NULL COMMENT '다발지역폴리곤',
                                             `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                             `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                             PRIMARY KEY (`id`),
                                             UNIQUE KEY `uk_hotspot_fid` (`accident_hotspot_fid`),
                                             KEY `idx_hotspot_fid` (`accident_hotspot_fid`),
                                             KEY `idx_hotspot_id` (`accident_hotspot_id`),
                                             KEY `idx_sido_code` (`sido_code`),
                                             KEY `idx_point_code` (`point_code`),
                                             KEY `idx_location` (`longitude`,`latitude`),
                                             KEY `idx_legal_dong` (`legal_dong`)
) ENGINE=InnoDB AUTO_INCREMENT=903 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='휴일 사고 다발지 데이터';



-- safe_walk.local_government_accident_hotspots definition

CREATE TABLE `local_government_accident_hotspots` (
                                                      `id` bigint NOT NULL AUTO_INCREMENT,
                                                      `accident_hotspot_fid` bigint NOT NULL COMMENT '사고다발지fid',
                                                      `accident_hotspot_id` bigint NOT NULL COMMENT '사고다발지id',
                                                      `sido_code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '법정동코드',
                                                      `point_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '지점코드',
                                                      `sido_sigungu_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '시도시군구명',
                                                      `point_name` text COLLATE utf8mb4_unicode_ci COMMENT '지점명',
                                                      `legal_dong` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '법정동',
                                                      `accident_count` int DEFAULT NULL COMMENT '사고건수',
                                                      `casualty_count` int DEFAULT NULL COMMENT '사상자수',
                                                      `death_count` int DEFAULT NULL COMMENT '사망자수',
                                                      `serious_injury_count` int DEFAULT NULL COMMENT '중상자수',
                                                      `minor_injury_count` int DEFAULT NULL COMMENT '경상자수',
                                                      `injury_report_count` int DEFAULT NULL COMMENT '부상신고자수',
                                                      `longitude` decimal(12,9) DEFAULT NULL COMMENT '경도',
                                                      `latitude` decimal(12,9) DEFAULT NULL COMMENT '위도',
                                                      `hotspot_polygon` json DEFAULT NULL COMMENT '다발지역폴리곤',
                                                      `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                                      `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                      PRIMARY KEY (`id`),
                                                      UNIQUE KEY `uk_hotspot_fid` (`accident_hotspot_fid`),
                                                      KEY `idx_hotspot_fid` (`accident_hotspot_fid`),
                                                      KEY `idx_hotspot_id` (`accident_hotspot_id`),
                                                      KEY `idx_sido_code` (`sido_code`),
                                                      KEY `idx_point_code` (`point_code`),
                                                      KEY `idx_location` (`longitude`,`latitude`),
                                                      KEY `idx_legal_dong` (`legal_dong`)
) ENGINE=InnoDB AUTO_INCREMENT=9269 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='지방자치단체 사고 다발지 데이터';


-- safe_walk.pedestrian_accident_hotspots definition

CREATE TABLE `pedestrian_accident_hotspots` (
                                                `id` bigint NOT NULL AUTO_INCREMENT,
                                                `accident_hotspot_fid` bigint NOT NULL COMMENT '사고다발지fid',
                                                `accident_hotspot_id` bigint NOT NULL COMMENT '사고다발지id',
                                                `sido_code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '법정동코드',
                                                `point_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '지점코드',
                                                `sido_sigungu_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '시도시군구명',
                                                `point_name` text COLLATE utf8mb4_unicode_ci COMMENT '지점명',
                                                `legal_dong` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '법정동',
                                                `accident_count` int DEFAULT NULL COMMENT '사고건수',
                                                `casualty_count` int DEFAULT NULL COMMENT '사상자수',
                                                `death_count` int DEFAULT NULL COMMENT '사망자수',
                                                `serious_injury_count` int DEFAULT NULL COMMENT '중상자수',
                                                `minor_injury_count` int DEFAULT NULL COMMENT '경상자수',
                                                `injury_report_count` int DEFAULT NULL COMMENT '부상신고자수',
                                                `longitude` decimal(12,9) DEFAULT NULL COMMENT '경도',
                                                `latitude` decimal(12,9) DEFAULT NULL COMMENT '위도',
                                                `hotspot_polygon` json DEFAULT NULL COMMENT '다발지역폴리곤',
                                                `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                                `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                PRIMARY KEY (`id`),
                                                UNIQUE KEY `uk_hotspot_fid` (`accident_hotspot_fid`),
                                                KEY `idx_hotspot_fid` (`accident_hotspot_fid`),
                                                KEY `idx_hotspot_id` (`accident_hotspot_id`),
                                                KEY `idx_sido_code` (`sido_code`),
                                                KEY `idx_point_code` (`point_code`),
                                                KEY `idx_location` (`longitude`,`latitude`),
                                                KEY `idx_legal_dong` (`legal_dong`)
) ENGINE=InnoDB AUTO_INCREMENT=4615 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='보행자 사고 다발지 데이터';



-- safe_walk.pedestrian_accident_hotspots definition

CREATE TABLE `pedestrian_accident_hotspots` (
                                                `id` bigint NOT NULL AUTO_INCREMENT,
                                                `accident_hotspot_fid` bigint NOT NULL COMMENT '사고다발지fid',
                                                `accident_hotspot_id` bigint NOT NULL COMMENT '사고다발지id',
                                                `sido_code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '법정동코드',
                                                `point_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '지점코드',
                                                `sido_sigungu_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '시도시군구명',
                                                `point_name` text COLLATE utf8mb4_unicode_ci COMMENT '지점명',
                                                `legal_dong` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '법정동',
                                                `accident_count` int DEFAULT NULL COMMENT '사고건수',
                                                `casualty_count` int DEFAULT NULL COMMENT '사상자수',
                                                `death_count` int DEFAULT NULL COMMENT '사망자수',
                                                `serious_injury_count` int DEFAULT NULL COMMENT '중상자수',
                                                `minor_injury_count` int DEFAULT NULL COMMENT '경상자수',
                                                `injury_report_count` int DEFAULT NULL COMMENT '부상신고자수',
                                                `longitude` decimal(12,9) DEFAULT NULL COMMENT '경도',
                                                `latitude` decimal(12,9) DEFAULT NULL COMMENT '위도',
                                                `hotspot_polygon` json DEFAULT NULL COMMENT '다발지역폴리곤',
                                                `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                                `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                PRIMARY KEY (`id`),
                                                UNIQUE KEY `uk_hotspot_fid` (`accident_hotspot_fid`),
                                                KEY `idx_hotspot_fid` (`accident_hotspot_fid`),
                                                KEY `idx_hotspot_id` (`accident_hotspot_id`),
                                                KEY `idx_sido_code` (`sido_code`),
                                                KEY `idx_point_code` (`point_code`),
                                                KEY `idx_location` (`longitude`,`latitude`),
                                                KEY `idx_legal_dong` (`legal_dong`)
) ENGINE=InnoDB AUTO_INCREMENT=4615 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='보행자 사고 다발지 데이터';


-- safe_walk.risk_areas definition

CREATE TABLE `risk_areas` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `year_code` int NOT NULL COMMENT '연도코드',
                              `sigungu_code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '시군구코드',
                              `sigungu_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '시군구명',
                              `risk_area_name` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '사고위험지역명',
                              `risk_area_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '사고위험지역',
                              `risk_area_polygon` text COLLATE utf8mb4_unicode_ci COMMENT '사고위험지역폴리곤',
                              `total_accident_count` int DEFAULT NULL COMMENT '총사고건수',
                              `total_death_count` int DEFAULT NULL COMMENT '총사망자수',
                              `total_serious_injury_count` int DEFAULT NULL COMMENT '총중상자수',
                              `total_minor_injury_count` int DEFAULT NULL COMMENT '총경상자수',
                              `total_injury_report_count` int DEFAULT NULL COMMENT '총부상신고자수',
                              `accident_analysis_type` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '사고분석유형명',
                              `center_utmk_x` decimal(15,4) DEFAULT NULL COMMENT '중심점utmkx좌표',
                              `center_utmk_y` decimal(15,4) DEFAULT NULL COMMENT '중심점utmky좌표',
                              `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                              `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_risk_area` (`year_code`,`sigungu_code`,`risk_area_code`),
                              KEY `idx_year` (`year_code`),
                              KEY `idx_sigungu` (`sigungu_code`),
                              KEY `idx_risk_area` (`risk_area_code`),
                              KEY `idx_accident_count` (`total_accident_count`)
) ENGINE=InnoDB AUTO_INCREMENT=11959 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사고 위험지역 데이터';




-- safe_walk.visitor_boom_neighborhoods definition

CREATE TABLE `visitor_boom_neighborhoods` (
                                              `id` bigint NOT NULL AUTO_INCREMENT,
                                              `ranking` int DEFAULT NULL COMMENT '순위',
                                              `sido_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '시도명',
                                              `sigungu_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '시군구명',
                                              `administrative_dong` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '행정동명',
                                              `visitor_count` decimal(15,2) DEFAULT NULL COMMENT '관광객수',
                                              `last_year_visitor_count` decimal(15,2) DEFAULT NULL COMMENT '전년동기관광객수',
                                              `growth_rate` decimal(5,2) DEFAULT NULL COMMENT '증감율',
                                              `base_year_month` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '기준년월',
                                              `search_date` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '조회일자',
                                              `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                              `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                              PRIMARY KEY (`id`),
                                              KEY `idx_ranking` (`ranking`),
                                              KEY `idx_sido` (`sido_name`),
                                              KEY `idx_sigungu` (`sigungu_name`),
                                              KEY `idx_administrative_dong` (`administrative_dong`),
                                              KEY `idx_location` (`sido_name`,`sigungu_name`),
                                              KEY `idx_growth_rate` (`growth_rate`),
                                              KEY `idx_base_year_month` (`base_year_month`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='방문자 급등동네(내국인) 데이터';


