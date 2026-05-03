const API_URL = "/api/v1/movies";

const mockMovies = [
    {
        id: 1,
        title: "THE SILENT SHADOW",
        genre: "Crime",
        posterImageUrl: "https://images.unsplash.com/photo-1509281373149-e957c6296406",
    },
    {
        id: 2,
        title: "VOID VOYAGER",
        genre: "Sci-Fi",
        posterImageUrl: "https://images.unsplash.com/photo-1462331940025-496dfbfc7564",
    },
    {
        id: 3,
        title: "NEON DREAMS",
        genre: "Cyberpunk",
        posterImageUrl: "https://images.unsplash.com/photo-1497032628192-86f99bcd76bc",
    },
    {
        id: 4,
        title: "LAST SUNRISE",
        genre: "Drama",
        posterImageUrl: "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
    },
];

export const getMovies = async (params = {}) => {
    console.log("Mock API called with:", params);

    return new Promise((resolve) => {
        setTimeout(() => {
            let data = [...mockMovies];

            if (params.search) {
                data = data.filter(m =>
                    m.title.toLowerCase().includes(params.search.toLowerCase())
                );
            }

            if (params.genre) {
                data = data.filter(m => m.genre === params.genre);
            }

            resolve(data);
        }, 800);
    });
};

// export const getMovies = async (params = {}) => {
//     const query = new URLSearchParams(params).toString();
//     const res = await fetch(`${API_URL}?${query}`);

//     if (!res.ok) throw new Error("Failed to fetch movies");

//     return res.json();
// };