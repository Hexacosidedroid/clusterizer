import React, { useState, useEffect } from "react";
import { Table, Button } from "antd";

const Images = () => {
    const [images, setImages] = useState([]);

    useEffect(() => {
        fetch("/listOfImages")
            .then((res) => res.json())
            .then((data) => setImages(data))
            .catch((err) => console.error(err));
    }, []);

    const removeImage = (id) => {
        fetch(`/removeImage?id=${id}`, { method: "DELETE" })
            .then(() => setImages(images.filter((image) => image.id !== id)))
            .catch((err) => console.error(err));
    };

    const columns = [
        { title: "ID", dataIndex: "id", key: "id" },
        { title: "Repository", dataIndex: "repository", key: "repository" },
        { title: "Tag", dataIndex: "tag", key: "tag" },
        {
            title: "Actions",
            key: "actions",
            render: (_, record) => (
                <Button danger onClick={() => removeImage(record.id)}>
                    Remove
                </Button>
            ),
        },
    ];

    return <Table dataSource={images} columns={columns} rowKey="id" />;
};

export default Images;