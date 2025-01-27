import React, { useState, useEffect } from "react";
import { Table, Button } from "antd";

const Containers = () => {
    const [containers, setContainers] = useState([]);

    useEffect(() => {
        fetch("/listOfContainers?all=true")
            .then((res) => res.json())
            .then((data) => setContainers(data))
            .catch((err) => console.error(err));
    }, []);

    const startContainer = (id) => {
        fetch(`/startContainer?id=${id}`, { method: "POST" })
            .then(() => console.log("Container started"))
            .catch((err) => console.error(err));
    };

    const removeContainer = (id) => {
        fetch(`/removeContainer?id=${id}&force=true`, { method: "DELETE" })
            .then(() => setContainers(containers.filter((container) => container.id !== id)))
            .catch((err) => console.error(err));
    };

    const columns = [
        { title: "ID", dataIndex: "id", key: "id" },
        { title: "Name", dataIndex: "name", key: "name" },
        { title: "State", dataIndex: "state", key: "state" },
        {
            title: "Actions",
            key: "actions",
            render: (_, record) => (
                <>
                    <Button onClick={() => startContainer(record.id)}>Start</Button>
                    <Button danger onClick={() => removeContainer(record.id)}>
                        Remove
                    </Button>
                </>
            ),
        },
    ];

    return <Table dataSource={containers} columns={columns} rowKey="id" />;
};

export default Containers;