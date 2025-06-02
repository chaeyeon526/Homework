create database if not exists game;
use game;

-- 과목 테이블
create table subject (
  id   int auto_increment primary key,
  name varchar(20) not null unique
);

insert into subject(name) values
  ('넌센스'),
  ('영어'),
  ('국어'),
  ('지리'),
  ('음악'),
  ('프로그래밍'),
  ('수학');

-- 문제 테이블
create table problem (
  id            int auto_increment primary key,
  class_order   tinyint not null,
  subject_id    int not null,
  question_text text not null,
  foreign key (subject_id) references subject(id) on delete cascade
);

insert into problem(class_order, subject_id, question_text) values
  (1, (select id from subject where name='넌센스'), '읽으면 읽을수록 지치는 책은?'),
  (1, (select id from subject where name='넌센스'), '들어갈 때 천명, 나올땐 백명인 것은?'),
  (2, (select id from subject where name='영어'), 'aggregation의 뜻은?'),
  (2, (select id from subject where name='영어'), 'pneumonoultramicroscopicsilicovolcanoconiosis의 뜻은?'),
  (3, (select id from subject where name='국어'), 'ㅁㅅㅁㅇㅅ?'),
  (3, (select id from subject where name='국어'), 'ㅁㅇㅇㅅㅋㅇ?'),
  (4, (select id from subject where name='지리'), '아이슬란드의 수도는?'),
  (4, (select id from subject where name='지리'), '몽골의 수도는?'),
  (5, (select id from subject where name='음악'), '점점 크게 라는 음악적 용어로, 음악의 강도가 점차적으로 증가하는 것을 나타내는 것은?'),
  (5, (select id from subject where name='음악'), '음악의 아버지는?(성 제외)'),
  (6, (select id from subject where name='프로그래밍'), '객체지향 4가지 요소 (다형성, 캡슐화, ___ , 추상)'),
  (6, (select id from subject where name='프로그래밍'), 'crud의 r은 무슨 단어의 약자일까요?'),
  (7, (select id from subject where name='수학'), '1부터 100까지 더한 값은?'),
  (7, (select id from subject where name='수학'), '(((99999 * 88888) / 123454321 + 543210 - 123456) * 246802 / 123401 + 77777 - 1000000) + 82572의 정답을 구하시오');

-- 정답 테이블
create table problem_answer (
  id         int auto_increment primary key,
  problem_id int not null,
  answer     varchar(5500) not null,
  foreign key (problem_id) references problem(id) on delete cascade
);

insert into problem_answer(problem_id, answer) values
  (1, '지침서'),
  (2, '인천아웃백'),
  (3, '총합'),
  (4, '진폐증'),
  (5, '무선마우스'),
  (6, 'MySQL'),
  (7, '레이캬비크'),
  (8, '울란바토르'),
  (9, '크레센도'),
  (10, '바흐'),
  (11, '상속'),
  (12, 'read'),
  (13, '5050'),
  (14, '1');

-- 인물 테이블
create table person (
  id   int auto_increment primary key,
  name varchar(300) not null
);

insert into person(name) values
  ('이민우'),
  ('김채연'),
  ('노현지'),
  ('오지환'),
  ('이윤기');

-- 인물⇄과목 매핑 테이블
create table person_subject (
  id         int auto_increment primary key,
  person_id  int not null,
  subject_id int not null,
  foreign key (person_id)  references person(id)  on delete cascade,
  foreign key (subject_id) references subject(id) on delete cascade
);

-- 매핑 데이터
insert into person_subject(person_id, subject_id) values
  ((select id from person where name='이민우'),
   (select id from subject where name='프로그래밍')),
  ((select id from person where name='이민우'),
   (select id from subject where name='국어')),
  ((select id from person where name='김채연'),
   (select id from subject where name='영어')),
  ((select id from person where name='김채연'),
   (select id from subject where name='국어')),
  ((select id from person where name='노현지'),
   (select id from subject where name='수학')),
  ((select id from person where name='노현지'),
   (select id from subject where name='음악')),
  ((select id from person where name='오지환'),
   (select id from subject where name='프로그래밍')),
  ((select id from person where name='오지환'),
   (select id from subject where name='음악')),
  ((select id from person where name='이윤기'),
   (select id from subject where name='지리')),
  ((select id from person where name='이윤기'),
   (select id from subject where name='영어'));

-- 선택된 인물이 아는 과목의 문제와 정답을 보여주는 뷰
create view auto_answers as
select
  p.name           as person_name,
  prob.id          as problem_id,
  s.name           as subject,
  prob.question_text,
  pa.answer        as correct_answer
from person p
join person_subject ps on ps.person_id = p.id
join subject s         on s.id = ps.subject_id
join problem prob      on prob.subject_id = s.id
join problem_answer pa on pa.problem_id = prob.id;
