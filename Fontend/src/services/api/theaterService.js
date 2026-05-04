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

// GET /v1/theaters
export const getTheaters = async () => {
    return new Promise((resolve) => {
        setTimeout(() => resolve([...MOCK_THEATERS]), 500);
    });
};

// GET /v1/theaters/{id}
export const getTheaterById = async (id) => {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            const theater = MOCK_THEATERS.find(t => t.id === Number(id));
            if (!theater) reject(new Error("Không tìm thấy rạp."));
            else resolve(theater);
        }, 500);
    });
};

// POST /v1/theaters
export const createTheater = async (theaterData) => {
    return new Promise((resolve) => {
        setTimeout(() => {
            const newTheater = {
                id: Math.max(...MOCK_THEATERS.map(t => t.id), 0) + 1,
                ...theaterData,
                capacity: Number(theaterData.capacity),
            };
            MOCK_THEATERS.push(newTheater);
            console.log("Theater created:", newTheater);
            resolve(newTheater);
        }, 500);
    });
};

// PUT /v1/theaters/{id}
export const updateTheater = async (id, theaterData) => {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            const index = MOCK_THEATERS.findIndex(t => t.id === Number(id));
            if (index === -1) {
                reject(new Error("Không tìm thấy rạp để cập nhật."));
                return;
            }
            const updatedTheater = {
                id: Number(id),
                ...theaterData,
                capacity: Number(theaterData.capacity),
            };
            MOCK_THEATERS[index] = updatedTheater;
            console.log("Theater updated:", updatedTheater);
            resolve(updatedTheater);
        }, 500);
    });
};

// DELETE /v1/theaters/{id}
export const deleteTheater = async (id) => {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            const index = MOCK_THEATERS.findIndex(t => t.id === Number(id));
            if (index === -1) {
                reject(new Error("Không tìm thấy rạp để xóa."));
                return;
            }
            const deletedTheater = MOCK_THEATERS.splice(index, 1)[0];
            console.log("Theater deleted:", deletedTheater);
            resolve(deletedTheater);
        }, 500);
    });
};

export const theaterService = {
    getAll: async () => {
        return getTheaters();
    },
};