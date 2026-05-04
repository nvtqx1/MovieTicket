// Sau này chỉ cần đổi mock → axios là xong

const MOCK_THEATERS = [
    {
        id: 1,
        name: "Lumière Landmark 72",
        location: "Hà Nội",
        capacity: 500,
        image: "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=800&q=80",
        type: "Premium",
    },
    {
        id: 2,
        name: "Lumière Bitexco",
        location: "TP. Hồ Chí Minh",
        capacity: 800,
        image: "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=800&q=80",
        type: "IMAX",
    },
    {
        id: 3,
        name: "Lumière Đà Nẵng",
        location: "Đà Nẵng",
        capacity: 450,
        image: null,
        type: "Standard",
    },
];

export const theaterService = {
    getAll: async () => {
        // giả lập API
        return new Promise((resolve) => {
            setTimeout(() => resolve(MOCK_THEATERS), 500);
        });

        // 👉 sau này:
        // return axios.get('/v1/theaters')
    },
};