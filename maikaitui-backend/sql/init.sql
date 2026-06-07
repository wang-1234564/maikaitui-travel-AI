-- Create database
CREATE DATABASE IF NOT EXISTS maikaitui DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE maikaitui;

-- ==================== System Tables ====================

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    nickname VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar VARCHAR(500),
    status TINYINT DEFAULT 1 COMMENT '0=disabled, 1=enabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    deleted TINYINT DEFAULT 0
) COMMENT 'User';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_desc VARCHAR(200),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    deleted TINYINT DEFAULT 0
) COMMENT 'Role';

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,
    menu_name VARCHAR(50) NOT NULL,
    menu_type VARCHAR(1) DEFAULT 'M' COMMENT 'M=menu, B=button',
    path VARCHAR(200),
    component VARCHAR(200),
    icon VARCHAR(100),
    sort_order INT DEFAULT 0,
    permission VARCHAR(200),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    deleted TINYINT DEFAULT 0
) COMMENT 'Menu';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) COMMENT 'User-Role';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) COMMENT 'Role-Menu';

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_name VARCHAR(100) NOT NULL,
    dict_type VARCHAR(100) NOT NULL UNIQUE,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    deleted TINYINT DEFAULT 0
) COMMENT 'Dict Type';

CREATE TABLE IF NOT EXISTS sys_dict_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_type VARCHAR(100) NOT NULL,
    dict_label VARCHAR(100) NOT NULL,
    dict_value VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0,
    css_class VARCHAR(100),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    deleted TINYINT DEFAULT 0,
    INDEX idx_dict_type (dict_type)
) COMMENT 'Dict Data';

CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    log_type VARCHAR(20) COMMENT 'operation/login',
    title VARCHAR(100),
    method VARCHAR(200),
    request_uri VARCHAR(500),
    remote_addr VARCHAR(50),
    user_agent VARCHAR(500),
    params TEXT,
    exception TEXT,
    execute_time BIGINT,
    username VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) COMMENT 'Operation Log';

-- ==================== Tourism Tables ====================

CREATE TABLE IF NOT EXISTS tourism_region (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    level INT DEFAULT 1 COMMENT '1=country,2=province,3=city,4=district',
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    deleted TINYINT DEFAULT 0
) COMMENT 'Region';

CREATE TABLE IF NOT EXISTS tourism_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(200),
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    deleted TINYINT DEFAULT 0
) COMMENT 'Category';

CREATE TABLE IF NOT EXISTS tourism_attraction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    region_id BIGINT,
    category_id BIGINT,
    address VARCHAR(500),
    latitude DOUBLE,
    longitude DOUBLE,
    price DECIMAL(10,2) DEFAULT 0.00,
    rating DOUBLE DEFAULT 5.0,
    cover_image VARCHAR(500),
    images TEXT COMMENT 'JSON array of image URLs',
    view_count BIGINT DEFAULT 0,
    like_count BIGINT DEFAULT 0,
    is_hot TINYINT DEFAULT 0,
    open_time VARCHAR(200),
    status TINYINT DEFAULT 1 COMMENT '1=published,0=draft',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    deleted TINYINT DEFAULT 0,
    INDEX idx_region_id (region_id),
    INDEX idx_category_id (category_id),
    INDEX idx_is_hot (is_hot)
) COMMENT 'Attraction';

CREATE TABLE IF NOT EXISTS tourism_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    attraction_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(50),
    avatar VARCHAR(500),
    content TEXT NOT NULL,
    rating INT DEFAULT 5,
    parent_id BIGINT DEFAULT 0,
    images TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    deleted TINYINT DEFAULT 0,
    INDEX idx_attraction_id (attraction_id),
    INDEX idx_user_id (user_id)
) COMMENT 'Comment';

CREATE TABLE IF NOT EXISTS tourism_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    attraction_id BIGINT NOT NULL,
    attraction_name VARCHAR(200),
    quantity INT DEFAULT 1,
    total_price DECIMAL(10,2) DEFAULT 0.00,
    order_status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/paid/cancelled/completed',
    visit_date DATE,
    contact_name VARCHAR(50),
    contact_phone VARCHAR(20),
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no)
) COMMENT 'Order';

CREATE TABLE IF NOT EXISTS tourism_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    attraction_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_user_attraction_deleted (user_id, attraction_id, deleted),
    INDEX idx_user_id (user_id),
    INDEX idx_attraction_id (attraction_id)
) COMMENT 'Favorite';

CREATE TABLE IF NOT EXISTS tourism_guide (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500),
    content LONGTEXT,
    cover_image VARCHAR(500),
    destination VARCHAR(100),
    region_id BIGINT,
    duration_days INT DEFAULT 1,
    budget_min DECIMAL(10,2) DEFAULT 0.00,
    budget_max DECIMAL(10,2) DEFAULT 0.00,
    season VARCHAR(50) COMMENT '春/夏/秋/冬/全年',
    travel_style VARCHAR(50) COMMENT '亲子/情侣/独自/朋友',
    attractions JSON COMMENT '关联景区ID列表',
    tips JSON COMMENT '旅行贴士 [{title, content}]',
    itinerary JSON COMMENT '行程结构 [{day, title, spots, hotel, meals}]',
    view_count BIGINT DEFAULT 0,
    like_count BIGINT DEFAULT 0,
    status TINYINT DEFAULT 1 COMMENT '0=draft,1=published',
    author_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    deleted TINYINT DEFAULT 0,
    INDEX idx_region_id (region_id),
    INDEX idx_destination (destination),
    INDEX idx_status (status),
    INDEX idx_duration (duration_days),
    INDEX idx_travel_style (travel_style)
) COMMENT 'Travel Guide';

-- ==================== Seed Data ====================

-- Users (password is BCrypt hash of '123456')
INSERT INTO sys_user (username, password, nickname, phone, email, status) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Super Admin', '13800138000', 'admin@maikaitui.com', 1),
('guide1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Guide Zhang', '13800138001', 'guide@maikaitui.com', 1),
('merchant1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Merchant Wang', '13800138002', 'merchant@maikaitui.com', 1);

-- Roles
INSERT INTO sys_role (role_name, role_code, role_desc, status) VALUES
('Super Admin', 'super_admin', 'Super admin with all permissions', 1),
('Admin', 'admin', 'System admin', 1),
('Tour Guide', 'tour_guide', 'Tour guide', 1),
('Merchant', 'merchant', 'Attraction merchant', 1),
('User', 'user', 'Normal user', 1);

-- User-Role mapping
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1), (2, 3), (3, 4);

-- Menus (14 menus)
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, icon, sort_order, permission, status) VALUES
(1, 0, 'System Mgmt', 'M', '/system', 'Layout', 'Setting', 1, '', 1),
(2, 1, 'User Mgmt', 'M', '/system/user', 'system/user/index', 'User', 1, 'sys:user:list', 1),
(3, 1, 'Role Mgmt', 'M', '/system/role', 'system/role/index', 'UserFilled', 2, 'sys:role:list', 1),
(4, 1, 'Menu Mgmt', 'M', '/system/menu', 'system/menu/index', 'Menu', 3, 'sys:menu:list', 1),
(5, 1, 'Dict Mgmt', 'M', '/system/dict', 'system/dict/index', 'Document', 4, 'sys:dict:list', 1),
(6, 1, 'Op Log', 'M', '/system/log', 'system/log/index', 'Tickets', 5, 'sys:log:list', 1),
(7, 0, 'Tourism Mgmt', 'M', '/tourism', 'Layout', 'Location', 2, '', 1),
(8, 7, 'Attractions', 'M', '/tourism/attraction', 'tourism/attraction/index', 'Picture', 1, 'tourism:attraction:list', 1),
(9, 7, 'Regions', 'M', '/tourism/region', 'tourism/region/index', 'MapLocation', 2, 'tourism:region:list', 1),
(10, 7, 'Categories', 'M', '/tourism/category', 'tourism/category/index', 'Collection', 3, 'tourism:category:list', 1),
(11, 7, 'Orders', 'M', '/tourism/order', 'tourism/order/index', 'Tickets', 4, 'tourism:order:list', 1),
(12, 7, 'Comments', 'M', '/tourism/comment', 'tourism/comment/index', 'ChatLineSquare', 5, 'tourism:comment:list', 1),
(13, 0, 'AI Console', 'M', '/ai', 'Layout', 'Cpu', 3, '', 1),
(14, 13, 'AI Chat', 'M', '/ai/chat', 'ai/chat/index', 'ChatDotRound', 1, 'ai:chat', 1);

-- Role-Menu (super_admin gets all)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- Regions
INSERT INTO tourism_region (id, name, parent_id, level, sort_order) VALUES
(1, 'China', 0, 1, 1),
(2, 'Beijing', 1, 2, 1),
(3, 'Shanghai', 1, 2, 2),
(4, 'Zhejiang', 1, 2, 3),
(5, 'Hangzhou', 4, 3, 1),
(6, 'Xihu District', 5, 4, 1),
(7, 'Sichuan', 1, 2, 4),
(8, 'Chengdu', 7, 3, 1),
(9, 'Yunnan', 1, 2, 5),
(10, 'Dali', 9, 3, 1),
(11, 'Hainan', 1, 2, 6),
(12, 'Sanya', 11, 3, 1);

-- Categories
INSERT INTO tourism_category (id, name, icon, parent_id, sort_order) VALUES
(1, 'Natural Scenery', 'sunny', 0, 1),
(2, 'Historical Sites', 'clock', 0, 2),
(3, 'Theme Parks', 'bicycle', 0, 3),
(4, 'Museums', 'notebook', 0, 4),
(5, 'Food Streets', 'fork-spoon', 0, 5),
(6, 'Nature Parks', 'sunny', 1, 1),
(7, 'Mountain & Water', 'sunset', 1, 2);

-- Attractions (8 seed attractions)
INSERT INTO tourism_attraction (name, description, region_id, category_id, address, latitude, longitude, price, rating, cover_image, is_hot, status) VALUES
('West Lake', 'West Lake is a famous scenic spot in Hangzhou, Zhejiang Province. It is one of the first national key scenic spots and top 10 scenic spots in China. The lake is surrounded by mountains on three sides and is renowned for its beautiful scenery and cultural heritage.', 6, 7, 'No.1 Longjing Road, Xihu District, Hangzhou, Zhejiang', 30.2592, 120.1320, 0.00, 4.8, 'https://picsum.photos/seed/westlake/800/400', 1, 1),
('Forbidden City', 'The Forbidden City, also known as the Palace Museum, is the imperial palace of the Ming and Qing dynasties. Located in the center of Beijing, it is one of the largest and best-preserved ancient wooden structures in the world.', 2, 2, 'No.4 Jingshan Front Street, Dongcheng District, Beijing', 39.9163, 116.3972, 60.00, 4.9, 'https://picsum.photos/seed/forbidden/800/400', 1, 1),
('Chengdu Panda Base', 'The Chengdu Research Base of Giant Panda Breeding is a world-renowned conservation and research facility for giant pandas. Visitors can observe pandas in a naturalistic habitat and learn about conservation efforts.', 8, 1, 'No.1375 Panda Avenue, Chenghua District, Chengdu, Sichuan', 30.7367, 104.1503, 55.00, 4.7, 'https://picsum.photos/seed/panda/800/400', 1, 1),
('Dali Old Town', 'Dali Old Town is located in Dali City, Yunnan Province. Its history dates back to the Tang Dynasty. It is one of the first 24 historical and cultural cities announced in 1982, known for its ancient architecture and Bai ethnic culture.', 10, 2, 'Fuxing Road, Dali, Yunnan', 25.6102, 100.2590, 0.00, 4.6, 'https://picsum.photos/seed/dali/800/400', 1, 1),
('Sanya Yalong Bay', 'Yalong Bay is located in Sanya, Hainan Province. It is a crescent-shaped bay at the southernmost tip of Hainan, known as "The First Bay Under Heaven" with its crystal-clear waters and white sandy beaches.', 12, 1, 'Yalong Bay National Resort, Sanya, Hainan', 18.2206, 109.6365, 0.00, 4.5, 'https://picsum.photos/seed/sanya/800/400', 1, 1),
('Shanghai Bund', 'The Bund is located on the banks of the Huangpu River in Huangpu District, Shanghai. It is a true portrayal of Shanghai\'s history and the starting point of the old Shanghai Concession era.', 3, 2, 'Zhongshan East 1st Road, Huangpu District, Shanghai', 31.2400, 121.4900, 0.00, 4.7, 'https://picsum.photos/seed/bund/800/400', 1, 1),
('Great Wall (Badaling)', 'Badaling Great Wall is a mountain pass of the Ming Great Wall and an important outpost of Juyong Pass. It is one of the most famous and best-preserved sections of the Great Wall.', 2, 2, 'G6 Beijing-Tibet Expressway Exit 58, Yanqing District, Beijing', 40.3551, 116.0140, 40.00, 4.8, 'https://picsum.photos/seed/greatwall/800/400', 1, 1),
('Lingyin Temple', 'Lingyin Temple, also known as Yunlin Temple, is located in Hangzhou, Zhejiang. Backed by Beigao Peak and facing Feilai Peak, it was first built in the first year of Xianhe in the Eastern Jin Dynasty.', 6, 2, 'No.1 Fayun Lane, Lingyin Road, Xihu District, Hangzhou', 30.2435, 120.0997, 45.00, 4.6, 'https://picsum.photos/seed/lingyin/800/400', 0, 1);

-- Guides (4 seed guides)
INSERT INTO tourism_guide (title, summary, content, cover_image, destination, region_id, duration_days, budget_min, budget_max, season, travel_style, attractions, tips, itinerary, view_count, status, author_id) VALUES
('杭州3天2晚深度游：从西湖到灵隐寺', '一次慢节奏的杭州之旅，涵盖了西湖晨跑、龙井村品茶、灵隐寺寻幽，适合周末短途出行。', '# 杭州3天2晚深度游

## 第一天：西湖环湖

早上8点到达杭州萧山机场，乘坐地铁1号线到龙翔桥站，步行5分钟即可到达西湖。

### 上午：断桥 → 白堤 → 孤山

从断桥残雪出发，沿着白堤一路走到孤山。春天的白堤两旁桃红柳绿，最适合散步。路程约2公里，边走边拍照大约1.5小时。

> **小贴士**：西湖早上7-9点人最少，光线也最适合拍照。建议早起避开人流。

### 下午：三潭印月 → 花港观鱼

在湖滨码头乘船前往三潭印月（船票55元），感受西湖十景之首。返程可以在花港观鱼码头下船，顺路游览。

### 晚上：南山路美食街

推荐尝试：**楼外楼**（西湖醋鱼、东坡肉）、**知味观**（猫耳朵、片儿川）

## 第二天：灵隐寺 → 龙井村

### 上午：灵隐寺寻幽

乘公交7路到灵隐寺站。门票45元。建议先逛飞来峰造像，再进寺庙参观。

> **小贴士**：灵隐寺的素面非常有名，建议中午在寺内素斋馆用午餐。

### 下午：龙井村品茶

从灵隐寺打车约20分钟到龙井村。可以参观茶园，在老茶农家里品尝正宗的明前龙井。

## 第三天：西溪湿地 → 返程

### 上午：西溪湿地泛舟

地铁到西溪湿地南站。建议乘坐摇橹船（60元/人），比电瓶船更有韵味。

> **小贴士**：《非诚勿扰》的取景地就在西溪湿地，可以打卡电影同款场景。

### 下午：返程

西溪湿地距离杭州东站约40分钟地铁，建议预留2小时交通时间。', 'https://picsum.photos/seed/guide-hangzhou/800/400', '杭州', 5, 3, 800, 2000, '春', '情侣', '[1, 8]', '[{"title": "住宿推荐", "content": "建议住在西湖区湖滨商圈附近，距离西湖步行5分钟内，价格200-500元/晚"}, {"title": "交通建议", "content": "杭州地铁覆盖主要景点，建议购买地铁一日票（15元/天）"}]', '[{"day": 1, "title": "西湖环湖", "spots": ["断桥残雪", "白堤", "孤山", "三潭印月", "花港观鱼"], "hotel": "西湖区湖滨商圈", "meals": ["楼外楼", "知味观"]}, {"day": 2, "title": "灵隐寻幽", "spots": ["灵隐寺", "飞来峰", "龙井村"], "hotel": "西湖区湖滨商圈", "meals": ["灵隐寺素斋", "龙井村农家菜"]}, {"day": 3, "title": "湿地泛舟", "spots": ["西溪湿地"], "hotel": "-", "meals": ["西溪湿地内餐厅"]}]', 3290, 1, 1),

('成都4天3晚：熊猫、火锅与慢生活', '来成都感受最地道的巴蜀文化，看大熊猫、吃火锅、逛宽窄巷子，体验悠闲的天府之国。', '# 成都4天3晚：熊猫、火锅与慢生活

## 第一天：抵达成都

下午到达成都双流机场，地铁10号线转3号线到春熙路，入住酒店。

### 晚上：春熙路 → IFS → 太古里

成都最繁华的商业区。IFS的熊猫屁股是网红打卡点，太古里的方所书店值得一逛。

晚餐推荐：**小龙坎老火锅**（春熙路店），人均80元。

## 第二天：大熊猫基地

### 上午：成都大熊猫繁育研究基地

建议7:30前到达，因为熊猫在早上最活跃（9点以后就开始睡觉了）。门票55元。

> **小贴士**：基地很大，建议乘坐观光车（10元），不然走断腿。月亮产房是最值得看的地方。

### 下午：宽窄巷子

回到市区逛宽窄巷子，三条巷子各有特色：宽巷子看建筑，窄巷子吃小吃，井巷子喝盖碗茶。

## 第三天：都江堰一日游

从犀浦站坐城际列车到都江堰（30分钟，10元）。游览这座2000多年前修建的水利工程奇迹。

### 晚上：九眼桥酒吧街

回成都后去九眼桥感受成都的夜生活。

## 第四天：人民公园 → 返程

上午去人民公园的鹤鸣茶社喝盖碗茶，体验成都人的慢生活。

> **小贴士**：可以请掏耳朵师傅来一次"舒耳"体验（30元），是成都特有的享受。

下午返程。', 'https://picsum.photos/seed/guide-chengdu/800/400', '成都', 8, 4, 1500, 3500, '秋', '朋友', '[3]', '[{"title": "住宿建议", "content": "春熙路/太古里商圈附近，交通便利，价格200-600元/晚"}, {"title": "必吃美食", "content": "火锅（小龙坎/大龙燚）、串串香、冒菜、钵钵鸡、赖汤圆、钟水饺"}]', '[{"day": 1, "title": "初见成都", "spots": ["春熙路", "IFS", "太古里"], "hotel": "春熙路商圈", "meals": ["小龙坎老火锅"]}, {"day": 2, "title": "熊猫之旅", "spots": ["成都大熊猫基地", "宽窄巷子"], "hotel": "春熙路商圈", "meals": ["宽窄巷子小吃"]}, {"day": 3, "title": "问道都江堰", "spots": ["都江堰", "九眼桥"], "hotel": "春熙路商圈", "meals": ["都江堰江边鱼庄", "九眼桥夜市"]}, {"day": 4, "title": "慢生活", "spots": ["人民公园", "鹤鸣茶社"], "hotel": "-", "meals": ["人民公园附近老字号"]}]', 2180, 1, 1),

('北京5天4晚：故宫长城全攻略', '囊括故宫、长城、颐和园等北京必打卡景点，合理的行程安排让你不错过任何经典。', '# 北京5天4晚：故宫长城全攻略

## 第一天：天安门 → 故宫

### 上午：天安门广场升旗

建议提前查好升旗时间（每天不同），提前1小时到达安检口。

### 上午：故宫博物院

从天安门进入故宫。门票60元（需提前7天预约！）。

> **重要提示**：故宫周一闭馆！务必提前在官网或小程序预约门票，旺季非常难抢。

建议游览路线：午门 → 太和殿 → 中和殿 → 保和殿 → 乾清宫 → 坤宁宫 → 御花园 → 神武门。全程约3-4小时。

### 晚上：南锣鼓巷

从故宫出来步行或骑车到南锣鼓巷，逛胡同吃小吃。

## 第二天：八达岭长城

### 全天：长城一日游

从德胜门乘坐877路直达八达岭长城（约1.5小时）。门票40元。

> **小贴士**：建议选择北线（人相对少一些），带足水和零食，穿运动鞋。缆车可以节省体力但不是必须的。

## 第三天：颐和园 → 圆明园

上午游览颐和园（门票30元），下午步行到隔壁的圆明园。

## 第四天：天坛 → 798艺术区

上午天坛公园（门票15元），下午798艺术区拍照。

## 第五天：雍和宫 → 返程

上午雍和宫烧香祈福，下午返程。', 'https://picsum.photos/seed/guide-beijing/800/400', '北京', 2, 5, 2500, 5000, '秋', '独自', '[2, 7]', '[{"title": "住宿推荐", "content": "建议住在前门或东单附近，靠近天安门和故宫，地铁出行方便，价格300-800元/晚"}, {"title": "门票预约", "content": "故宫、国博等热门景点需提前预约。建议出发前一周就开始预约"}]', '[{"day": 1, "title": "皇城中轴线", "spots": ["天安门", "故宫", "南锣鼓巷"], "hotel": "前门/东单商圈", "meals": ["南锣鼓巷小吃"]}, {"day": 2, "title": "万里长城", "spots": ["八达岭长城"], "hotel": "前门/东单商圈", "meals": ["自带干粮", "市区晚餐"]}, {"day": 3, "title": "皇家园林", "spots": ["颐和园", "圆明园"], "hotel": "前门/东单商圈", "meals": ["颐和园附近餐厅"]}, {"day": 4, "title": "艺术北京", "spots": ["天坛", "798艺术区"], "hotel": "前门/东单商圈", "meals": ["798内餐厅"]}, {"day": 5, "title": "祈福告别", "spots": ["雍和宫"], "hotel": "-", "meals": ["雍和宫附近老北京炸酱面"]}]', 4520, 1, 1),

('大理丽江6天5晚：云南风花雪月之旅', '一次走遍大理丽江两个云南最经典的旅游目的地，适合第一次去云南的朋友。', '# 大理丽江6天5晚：云南风花雪月之旅

## 第一天：抵达大理

飞机到大理机场或火车到大理站。入住大理古城内的客栈。

### 晚上：大理古城漫步

在古城内闲逛，感受白族文化与现代文艺的结合。人民路是最热闹的地方。

> **小贴士**：大理海拔约2000米，一般不会有高原反应，但紫外线较强，注意防晒。

## 第二天：洱海环湖

租一辆电动车（约50元/天）环洱海。推荐路线：古城 → 喜洲古镇 → 双廊 → 挖色 → 小普陀。

### 喜洲古镇

白族建筑保存最完好的古镇，喜洲粑粑必吃。

### 双廊

杨丽萍的太阳宫所在地，是拍照最美的路段。

## 第三天：苍山 → 丽江

上午坐索道上苍山（洗马潭索道/感通索道），下午乘火车去丽江（约2小时）。

## 第四天：玉龙雪山

报一日游团或自行前往玉龙雪山（门票100元+索道120元）。大索道到海拔4506米。

> **重要提示**：玉龙雪山海拔较高，建议提前准备氧气瓶（古城药店15元/瓶），山上买贵很多。

### 晚上：丽江古城

丽江古城比大理古城大很多，夜景非常美。大水车、四方街是必打卡点。

## 第五天：束河古镇 + 白沙古镇

上午束河古镇（比丽江古城安静，商业化程度低），下午白沙古镇看壁画。

## 第六天：黑龙潭 → 返程

上午去黑龙潭公园看玉龙雪山倒影（天气好的话），下午返程。', 'https://picsum.photos/seed/guide-yunnan/800/400', '大理/丽江', 10, 6, 3000, 6000, '夏', '情侣', '[4]', '[{"title": "住宿推荐", "content": "大理住古城内客栈（200-400元/晚），丽江住古城北门附近（避开酒吧街以免吵闹），价格200-500元/晚"}, {"title": "购物建议", "content": "鲜花饼、普洱茶、扎染工艺品值得买。但不要在景区买银器和翡翠，价格虚高"}, {"title": "注意事项", "content": "云南早晚温差大，即使是夏天也要带一件薄外套。紫外线强，防晒霜+墨镜+帽子必备"}]', '[{"day": 1, "title": "初见大理", "spots": ["大理古城", "人民路"], "hotel": "大理古城客栈", "meals": ["古城内白族餐厅"]}, {"day": 2, "title": "环游洱海", "spots": ["喜洲古镇", "双廊", "挖色", "小普陀"], "hotel": "大理古城客栈", "meals": ["喜洲粑粑", "双廊湖景餐厅"]}, {"day": 3, "title": "苍山转场", "spots": ["苍山", "丽江古城"], "hotel": "丽江古城北门附近", "meals": ["丽江腊排骨火锅"]}, {"day": 4, "title": "雪山之巅", "spots": ["玉龙雪山", "丽江古城"], "hotel": "丽江古城北门附近", "meals": ["古城内纳西餐厅"]}, {"day": 5, "title": "慢游古镇", "spots": ["束河古镇", "白沙古镇"], "hotel": "丽江古城北门附近", "meals": ["白沙古镇农家菜"]}, {"day": 6, "title": "告别云南", "spots": ["黑龙潭公园"], "hotel": "-", "meals": ["丽江最后一餐"]}]', 5890, 1, 1);

-- Dict seed data
INSERT INTO sys_dict_type (dict_name, dict_type, status) VALUES
('Order Status', 'order_status', 1),
('Attraction Status', 'attraction_status', 1);

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort_order) VALUES
('order_status', 'Pending', 'pending', 1),
('order_status', 'Paid', 'paid', 2),
('order_status', 'Completed', 'completed', 3),
('order_status', 'Cancelled', 'cancelled', 4),
('attraction_status', 'Published', '1', 1),
('attraction_status', 'Draft', '0', 2);
