const API_URL = "/api/v1/movies";

// GET /v1/movies — Danh sách phim (Home page)
const mockMovies = [
    {
        id: 1,
        title: "THE SILENT SHADOW",
        genre: "Crime",
        releaseYear: 2024,
        posterImageUrl: "https://images.unsplash.com/photo-1509281373149-e957c6296406",
    },
    {
        id: 2,
        title: "VOID VOYAGER",
        genre: "Sci-Fi",
        releaseYear: 2025,
        posterImageUrl: "https://images.unsplash.com/photo-1462331940025-496dfbfc7564",
    },
    {
        id: 3,
        title: "NEON DREAMS",
        genre: "Cyberpunk",
        releaseYear: 2025,
        posterImageUrl: "https://images.unsplash.com/photo-1497032628192-86f99bcd76bc",
    },
    {
        id: 4,
        title: "LAST SUNRISE",
        genre: "Drama",
        releaseYear: 2024,
        posterImageUrl: "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
    },
];

// GET /v1/movies/{id} — Chi tiết phim
// id, title, genre, posterImageUrl, description, releaseYear
const mockMovieDetails = {
    1: {
        id: 1,
        title: "THE SILENT SHADOW",
        genre: "Crime",
        posterImageUrl: "https://images.unsplash.com/photo-1509281373149-e957c6296406",
        description: "Một thám tử kỳ cựu bị cuốn vào vụ án bí ẩn khi những tội ác trong quá khứ dần được hé lộ.",
        releaseYear: 2024,
    },
    2: {
        id: 2,
        title: "VOID VOYAGER",
        genre: "Sci-Fi",
        posterImageUrl: "https://images.unsplash.com/photo-1462331940025-496dfbfc7564",
        description: "Hành trình xuyên không gian của một phi hành gia cô đơn tìm kiếm ý nghĩa sự tồn tại.",
        releaseYear: 2025,
    },
    3: {
        id: 3,
        title: "NEON DREAMS",
        genre: "Cyberpunk",
        posterImageUrl: "https://images.unsplash.com/photo-1497032628192-86f99bcd76bc",
        description: "Trong siêu đô thị rực rỡ ánh đèn neon, một hacker trẻ phát hiện âm mưu thay đổi thực tại.",
        releaseYear: 2025,
    },
    4: {
        id: 4,
        title: "LAST SUNRISE",
        genre: "Drama",
        posterImageUrl: "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
        description: "Câu chuyện cảm động về tình người trong những ngày cuối cùng của một cuộc đời.",
        releaseYear: 2024,
    },
};

// GET /v1/showtimes?movieId={id}
// showDate, showTime, price, isFlashSale
//
const mockShowtimes = [
    { id: 101, movieId: 1, showDate: "2026-10-24", showTime: "14:20", price: 80000, isFlashSale: false },
    { id: 102, movieId: 1, showDate: "2026-10-24", showTime: "17:45", price: 90000, isFlashSale: true },
    { id: 103, movieId: 1, showDate: "2026-10-25", showTime: "21:00", price: 90000, isFlashSale: false },
    { id: 201, movieId: 2, showDate: "2026-10-24", showTime: "15:30", price: 85000, isFlashSale: false },
    { id: 202, movieId: 2, showDate: "2026-10-25", showTime: "19:30", price: 120000, isFlashSale: true },
    { id: 301, movieId: 3, showDate: "2026-10-24", showTime: "18:15", price: 90000, isFlashSale: false },
    { id: 401, movieId: 4, showDate: "2026-10-25", showTime: "14:00", price: 80000, isFlashSale: false },
];

// ==========================================
// API FUNCTIONS
// ==========================================

// GET /v1/movies
export const getMovies = async (params = {}) => {
    console.log("Mock API called with:", params);

    return new Promise((resolve) => {
        setTimeout(() => {
            let data = [...mockMovies];

            if (params.search) {
                data = data.filter((m) =>
                    m.title.toLowerCase().includes(params.search.toLowerCase())
                );
            }
            if (params.genre) {
                data = data.filter((m) => m.genre === params.genre);
            }
            if (params.releaseYear) {
                data = data.filter((m) => m.releaseYear === Number(params.releaseYear));
            }

            resolve(data);
        }, 800);
    });

    // const query = new URLSearchParams(params).toString();
    // const res = await fetch(`${API_URL}?${query}`);
    // if (!res.ok) throw new Error("Failed to fetch movies");
    // return res.json();
};

// GET /v1/movies/{id}
export const getMovieById = async (id) => {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            const movie = mockMovieDetails[id];
            if (!movie) reject(new Error("Không tìm thấy phim."));
            else resolve(movie);
        }, 600);
    });

    // const res = await fetch(`${API_URL}/${id}`);
    // if (!res.ok) throw new Error("Không tìm thấy phim.");
    // return res.json();
};

// GET /v1/showtimes?movieId={id}
export const getShowtimesByMovieId = async (movieId) => {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve(mockShowtimes.filter((st) => st.movieId === Number(movieId)));
        }, 600);
    });

    // const res = await fetch(`/api/v1/showtimes?movieId=${movieId}`);
    // if (!res.ok) throw new Error("Failed to fetch showtimes");
    // return res.json();
};
