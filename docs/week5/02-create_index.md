## 인덱스 생성

### 브랜드 ID + 좋아요 DESC 복합 인덱스 생성

##### 1) 기본 (cost=10118,actual time=4.77..114)

EXPLAIN ANALYZE SELECT *
FROM product_list_view WHERE brand_id = '9';

##### 기본

-> Filter: (product_list_view.brand_id = 9)  (cost=10118 rows=9950) (actual time=4.77..114 rows=2019 loops=1)
-> Table scan on product_list_view  (cost=10118 rows=99499) (actual time=4.74..110 rows=100000 loops=1)

##### 인텍스 생성 후

** idx_brand_like Index 생성 -> Index lookup on product_list_view using idx_brand_like (brand_id=9)  (cost=743
rows=2122) (actual time=2.2..65.3 rows=2122 loops=1)

##### 결과

idx_brand_like 인덱스 사용 후, 정렬이 실행되어 cost, 실행 시간이 개선되었습니다. cost: 10118 -> 743 actual time=4.77..114 -> 2.2..65.3

##### 2) 최신순 (cost=10118,actual time=102..102)

EXPLAIN ANALYZE SELECT *
FROM product_list_view WHERE brand_id = '9' ORDER BY created_at DESC ;

##### 기본

-> Sort: product_list_view.created_at DESC  (cost=10118 rows=99499) (actual time=102..102 rows=2019 loops=1)
-> Filter: (product_list_view.brand_id = 9)  (cost=10118 rows=99499) (actual time=0.573..96.7 rows=2019 loops=1)
-> Table scan on product_list_view  (cost=10118 rows=99499) (actual time=0.56..91.6 rows=100000 loops=1)

##### 인텍스 생성 후

** idx_brand_like 생성 -> Sort: product_list_view.created_at DESC  (cost=707 rows=2019) (actual time=2.22..2.36 rows=2019
loops=1)
-> Index lookup on product_list_view using idx_brand_like (brand_id=9)  (cost=707 rows=2019) (actual time=0.164..1.92
rows=2019 loops=1)

##### 결과

idx_brand_like 인덱스 사용 후, 정렬이 실행되어 cost, 실행 시간이 개선되었습니다. cost: 10118 -> 707 actual time=102..102 -> 2.22..2.36

##### 3) 좋아요순 (cost=10118,actual time=75.4..75.5)

EXPLAIN ANALYZE SELECT *
FROM product_list_view WHERE brand_id = '9' ORDER BY like_count DESC ;

##### 기본

-> Sort: product_list_view.like_count DESC  (cost=10118 rows=99499) (actual time=75.4..75.5 rows=2019 loops=1)
-> Filter: (product_list_view.brand_id = 9)  (cost=10118 rows=99499) (actual time=0.899..71.9 rows=2019 loops=1)
-> Table scan on product_list_view  (cost=10118 rows=99499) (actual time=0.889..67.8 rows=100000 loops=1)

##### 인텍스 생성 후

-> Index lookup on product_list_view using idx_brand_like (brand_id=9), with index condition: (
product_list_view.brand_id = 9)  (cost=707 rows=2019) (actual time=0.263..5.65 rows=2019 loops=1)

##### 결과

idx_brand_like 인덱스 사용 후, cost, 실행 시간이 개선되었습니다. cost: 10118 -> 707 actual time=75.4..75.5 -> 0.263..5.65

##### 4) 가격순 (cost=10118,actual time=141..141)

EXPLAIN ANALYZE SELECT *
FROM product_list_view WHERE brand_id = '9' ORDER BY price DESC ;

##### 기본

-> Sort: product_list_view.price DESC  (cost=10118 rows=99499) (actual time=141..141 rows=2019 loops=1)
-> Filter: (product_list_view.brand_id = 9)  (cost=10118 rows=99499) (actual time=5.04..134 rows=2019 loops=1)
-> Table scan on product_list_view  (cost=10118 rows=99499) (actual time=4.97..124 rows=100000 loops=1)

##### 인텍스 생성 후

-> Sort: product_list_view.price DESC  (cost=707 rows=2019) (actual time=1.97..1.98 rows=219 loops=1)
-> Index lookup on product_list_view using idx_brand_like (brand_id=9)  
, with index condition: (product_list_view.brand_id = 9)  (cost=707 rows=2019) (actual time=1.71..1.91 rows=219 loops=1)

##### 결과

idx_brand_like 인덱스 사용 후, 정렬이 실행되어 cost, 실행 시간이 개선되었습니다.  
cost: 10118 -> 707 actual time=141..141 -> 1.97..1.98

### Q. 숫자 타입 + 수정이 빈번한 좋아요 수를 인덱스에 넣어도 되는가?

### 브랜드 ID만으로 인덱스 생성시

##### 1) 좋아요순

EXPLAIN ANALYZE SELECT *
FROM product_list_view WHERE brand_id = '9' ORDER BY like_count DESC ;

##### 기본(cost=10118 actual time=75.4..75.5)

-> Sort: product_list_view.like_count DESC  (cost=10118 rows=99499) (actual time=75.4..75.5 rows=2019 loops=1)
-> Filter: (product_list_view.brand_id = 9)  (cost=10118 rows=99499) (actual time=0.899..71.9 rows=2019 loops=1)
-> Table scan on product_list_view  (cost=10118 rows=99499) (actual time=0.889..67.8 rows=100000 loops=1)

##### 브랜드 ID + 좋아요 DESC 복합 인덱스 (cost=707 actual time=0.263..5.65)

-> Index lookup on product_list_view using idx_brand_like (brand_id=9), with index condition: (
product_list_view.brand_id = 9)  (cost=707 rows=2019) (actual time=0.263..5.65 rows=2019 loops=1)

##### 브랜드 ID 인덱스(cost=707 actual time=7.39..7.64)

-> Sort: product_list_view.like_count DESC  (cost=707 rows=2019) (actual time=7.39..7.64 rows=2019 loops=1)
-> Index lookup on product_list_view using idx_brand (brand_id=9), with index condition: (product_list_view.brand_id =

9) (cost=707 rows=2019) (actual time=0.389..6.34 rows=2019 loops=1)

### 좋아요수에 반복 업데이트 실행(cost=10942, actual time=0.149..109)

특정 브랜드의 상품수 10만건을 늘리고, 좋아요수 업데이트를 진행했지만, 인덱스 성능이 낮아짐을 느끼지 못했습니다.  
전체 상픔의 좋어요수 정렬에서도 큰 차이가 없었고, 한 브랜드의 상픔이 10만건이 될 경우도 비현실적이라고 생각되었습니다.  
때문에 분석 전에는 브랜드 ID 인덱스만 사용하려고 했으나, 브랜드 ID + 좋아요수 DESC 인덱스를 사용하기로 했습니다.

```
WITH RECURSIVE nums AS (
SELECT 1 AS n
UNION ALL
SELECT n + 1 FROM nums WHERE n < 100
)
UPDATE product_list_view p
JOIN nums n ON TRUE
SET p.like_count = p.like_count + FLOOR(RAND() * 100 - 50)
where brand_id = 9
```

-> Index lookup on product_list_view using idx_brand_like (brand_id=9), with index condition: (
product_list_view.brand_id = 9)  (cost=10942 rows=99560) (actual time=0.149..109 rows=102019 loops=1)

